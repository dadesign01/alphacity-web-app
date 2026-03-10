'use client';

import { Plus, Upload, Edit, Trash2, X, Save } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';

interface StampItem {
  id: number;
  name: string;
  conditionType: string;
  conditionDetail: string | null;
  imageUrl: string | null;
  program: { id: number; name: string } | null;
  place: { id: number; name: string } | null;
  _count: { userStamps: number };
}

interface OptionItem { id: number; name: string; }

const CONDITION_MAP: Record<string, string> = {
  mission_complete: '미션 완료', event_participate: '이벤트 참여', place_visit: '장소 방문', quiz_correct: '퀴즈 정답',
};

const EMPTY_FORM = { name: '', conditionType: 'mission_complete', conditionDetail: '', programId: 0, placeId: 0 };

export default function StampsPage() {
  const [stamps, setStamps] = useState<StampItem[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [programs, setPrograms] = useState<OptionItem[]>([]);
  const [places, setPlaces] = useState<OptionItem[]>([]);
  const [imageUrl, setImageUrl] = useState('');
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const fetchStamps = () => {
    fetch('/api/v1/admin/stamps').then((r) => r.json()).then((d) => { if (d.success) setStamps(d.data); });
  };

  useEffect(() => {
    fetchStamps();
    fetch('/api/v1/admin/programs').then((r) => r.json()).then((d) => {
      if (d.success) setPrograms(d.data.map((p: OptionItem) => ({ id: p.id, name: p.name })));
    });
    fetch('/api/v1/admin/places').then((r) => r.json()).then((d) => {
      if (d.success) setPlaces(d.data.map((p: OptionItem) => ({ id: p.id, name: p.name })));
    });
  }, []);

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      const res = await fetch('/api/v1/admin/upload', { method: 'POST', body: formData });
      const json = await res.json();
      if (json.success) {
        setImageUrl(json.data.imageUrl);
      } else {
        alert(json.error?.message || '업로드 실패');
      }
    } catch {
      alert('업로드 중 오류가 발생했습니다');
    }
    setUploading(false);
  };

  const openCreateForm = () => {
    setEditingId(null);
    setForm(EMPTY_FORM);
    setImageUrl('');
    if (fileInputRef.current) fileInputRef.current.value = '';
    setShowForm(true);
  };

  const openEditForm = (stamp: StampItem) => {
    setEditingId(stamp.id);
    setForm({
      name: stamp.name,
      conditionType: stamp.conditionType,
      conditionDetail: stamp.conditionDetail || '',
      programId: stamp.program?.id || 0,
      placeId: stamp.place?.id || 0,
    });
    setImageUrl(stamp.imageUrl || '');
    setShowForm(true);
  };

  const handleSubmit = async () => {
    const isEdit = editingId !== null;
    const url = isEdit ? `/api/v1/admin/stamps/${editingId}` : '/api/v1/admin/stamps';
    const method = isEdit ? 'PUT' : 'POST';

    try {
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...form,
          programId: form.programId || null,
          placeId: form.placeId || null,
          imageUrl: imageUrl || null,
        }),
      });
      const data = await res.json();
      if (data.success) {
        setShowForm(false);
        setEditingId(null);
        setForm(EMPTY_FORM);
        setImageUrl('');
        fetchStamps();
      } else {
        alert(`${isEdit ? '수정' : '등록'} 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert(`${isEdit ? '수정' : '등록'} 실패: 서버와 통신할 수 없습니다`);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    try {
      const res = await fetch(`/api/v1/admin/stamps/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (data.success) {
        fetchStamps();
      } else {
        alert(`삭제 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('삭제 실패: 서버와 통신할 수 없습니다');
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">스탬프 관리</h2>
          <p className="text-sm text-gray-500 mt-1">스탬프 발급 조건을 설정합니다</p>
        </div>
        <button onClick={openCreateForm}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus className="w-4 h-4" />
          스탬프 추가
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              {editingId ? '스탬프 수정' : '스탬프 등록'}
            </h3>
            <button onClick={() => { setShowForm(false); setEditingId(null); }} className="text-gray-400 hover:text-gray-600">
              <X className="w-5 h-5" />
            </button>
          </div>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">스탬프명</label>
              <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
                placeholder="스탬프 이름을 입력하세요"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">연결된 프로그램</label>
                <select value={form.programId} onChange={(e) => setForm({ ...form, programId: Number(e.target.value) })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                  <option value={0}>선택 안 함</option>
                  {programs.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">연결된 장소</label>
                <select value={form.placeId} onChange={(e) => setForm({ ...form, placeId: Number(e.target.value) })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                  <option value={0}>선택 안 함</option>
                  {places.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
                </select>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">발급 조건 설정</label>
              <select value={form.conditionType} onChange={(e) => setForm({ ...form, conditionType: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                {Object.entries(CONDITION_MAP).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">상세 조건</label>
              <textarea value={form.conditionDetail} onChange={(e) => setForm({ ...form, conditionDetail: e.target.value })}
                placeholder="발급 조건에 대한 상세 설명을 입력하세요" rows={3}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">스탬프 이미지</label>
              <div className="flex items-center gap-4">
                <label className="border-2 border-dashed border-gray-300 rounded-lg p-4 text-center hover:border-blue-400 transition-colors cursor-pointer inline-block">
                  <Upload className="w-6 h-6 text-gray-400 mx-auto mb-1" />
                  <p className="text-sm text-gray-600">이미지 업로드</p>
                  <p className="text-xs text-gray-500 mt-0.5">PNG, JPG (최대 5MB)</p>
                  <input ref={fileInputRef} type="file" accept="image/*" onChange={handleUpload} className="hidden" />
                </label>
                {uploading && <span className="text-sm text-gray-500">업로드 중...</span>}
              </div>
              {imageUrl && (
                <div className="mt-3 flex items-center gap-3">
                  <img src={imageUrl} alt="미리보기" className="h-20 w-20 rounded-lg object-cover border border-gray-200" />
                  <button onClick={() => { setImageUrl(''); if (fileInputRef.current) fileInputRef.current.value = ''; }}
                    className="text-sm text-red-500 hover:text-red-700">삭제</button>
                </div>
              )}
            </div>
            <button onClick={handleSubmit}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
              <Save className="w-4 h-4" />
              {editingId ? '수정' : '등록'}
            </button>
          </div>
        </div>
      )}

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">등록된 스탬프</h3>
        </div>
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">스탬프명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">프로그램</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">장소</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">발급 조건</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">발급 수</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {stamps.map((stamp) => (
              <tr key={stamp.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-900">
                  <div className="flex items-center gap-3">
                    {stamp.imageUrl && (
                      <img src={stamp.imageUrl} alt={stamp.name} className="h-8 w-8 rounded object-cover" />
                    )}
                    {stamp.name}
                  </div>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">{stamp.program?.name || '-'}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{stamp.place?.name || '-'}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{CONDITION_MAP[stamp.conditionType] || stamp.conditionType}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{stamp._count.userStamps.toLocaleString()}개</td>
                <td className="px-6 py-4">
                  <div className="flex gap-2">
                    <button onClick={() => openEditForm(stamp)}
                      className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                      <Edit className="w-4 h-4" />
                    </button>
                    <button onClick={() => handleDelete(stamp.id)}
                      className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {stamps.length === 0 && (
              <tr><td colSpan={6} className="px-6 py-8 text-center text-sm text-gray-500">등록된 스탬프가 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
