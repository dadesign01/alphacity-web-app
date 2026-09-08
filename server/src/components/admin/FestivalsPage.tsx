'use client';

import { Plus, Edit, Trash2, X, Save, Upload, ChevronUp, ChevronDown } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';

interface Festival {
  id: number;
  name: string;
  description?: string | null;
  imageUrl?: string | null;
  bannerUrl?: string | null;
  startDate: string;
  endDate: string;
  latitude?: number | null;
  longitude?: number | null;
  address?: string | null;
  sortOrder: number;
  isActive: boolean;
  status: string;
  _count?: { programs: number; events: number; missions: number; stamps: number; coupons: number; stores: number };
}

const STATUS_MAP: Record<string, string> = {
  scheduled: '예정',
  in_progress: '진행중',
  ended: '종료',
};

const STATUS_COLOR: Record<string, string> = {
  scheduled: 'bg-gray-100 text-gray-800',
  in_progress: 'bg-green-100 text-green-800',
  ended: 'bg-red-100 text-red-800',
};

interface FormState {
  name: string;
  description: string;
  imageUrl: string;
  bannerUrl: string;
  startDate: string;
  endDate: string;
  latitude: string;
  longitude: string;
  address: string;
  sortOrder: string;
  isActive: boolean;
}

const initialForm: FormState = {
  name: '',
  description: '',
  imageUrl: '',
  bannerUrl: '',
  startDate: '',
  endDate: '',
  latitude: '',
  longitude: '',
  address: '',
  sortOrder: '0',
  isActive: true,
};

export default function FestivalsPage() {
  const [festivals, setFestivals] = useState<Festival[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [form, setForm] = useState<FormState>(initialForm);
  const [submitting, setSubmitting] = useState(false);
  const imageInputRef = useRef<HTMLInputElement>(null);
  const bannerInputRef = useRef<HTMLInputElement>(null);

  const fetchFestivals = async () => {
    try {
      setLoading(true);
      const res = await fetch('/api/v1/admin/festivals');
      const json = await res.json();
      if (json.success) setFestivals(json.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchFestivals(); }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm({ ...initialForm, sortOrder: String(festivals.length) });
    setShowForm(true);
  };

  const openEdit = (f: Festival) => {
    setEditingId(f.id);
    setForm({
      name: f.name,
      description: f.description ?? '',
      imageUrl: f.imageUrl ?? '',
      bannerUrl: f.bannerUrl ?? '',
      startDate: f.startDate.slice(0, 10),
      endDate: f.endDate.slice(0, 10),
      latitude: f.latitude != null ? String(f.latitude) : '',
      longitude: f.longitude != null ? String(f.longitude) : '',
      address: f.address ?? '',
      sortOrder: String(f.sortOrder),
      isActive: f.isActive,
    });
    setShowForm(true);
  };

  const closeForm = () => {
    setShowForm(false);
    setEditingId(null);
    setForm(initialForm);
  };

const uploadImage = async (file: File): Promise<string | null> => {
  const fd = new FormData();
  fd.append('file', file);

  try {
    const res = await fetch('/api/v1/upload', {
      method: 'POST',
      body: fd,
    });

    const json = await res.json();

    if (json.success && json.data?.imageUrl) {
      return json.data.imageUrl as string;
    }
  } catch (error) {
    console.error('이미지 업로드 오류:', error);
  }

  return null;
};

  const handleImageUpload = async (kind: 'imageUrl' | 'bannerUrl', e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const url = await uploadImage(file);
    if (url) setForm(prev => ({ ...prev, [kind]: url }));
    else alert('이미지 업로드에 실패했습니다.');
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.name || !form.startDate || !form.endDate) {
      alert('축제명, 시작일, 종료일은 필수입니다.');
      return;
    }
    setSubmitting(true);
    try {
      const payload = {
        name: form.name,
        description: form.description || null,
        imageUrl: form.imageUrl || null,
        bannerUrl: form.bannerUrl || null,
        startDate: form.startDate,
        endDate: form.endDate,
        latitude: form.latitude || null,
        longitude: form.longitude || null,
        address: form.address || null,
        sortOrder: Number(form.sortOrder) || 0,
        isActive: form.isActive,
      };
      const url = editingId ? `/api/v1/admin/festivals/${editingId}` : '/api/v1/admin/festivals';
      const method = editingId ? 'PUT' : 'POST';
      const res = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
      const json = await res.json();
      if (!json.success) {
        alert(json.error?.message ?? '저장에 실패했습니다.');
        return;
      }
      await fetchFestivals();
      closeForm();
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: number, name: string) => {
    if (!confirm(`'${name}' 축제를 삭제하면 연결된 행사·이벤트·미션·스탬프·쿠폰·상점이 모두 삭제됩니다. 계속할까요?`)) return;
    const res = await fetch(`/api/v1/admin/festivals/${id}`, { method: 'DELETE' });
    const json = await res.json();
    if (!json.success) {
      alert(json.error?.message ?? '삭제에 실패했습니다.');
      return;
    }
    await fetchFestivals();
  };

  const reorder = async (id: number, direction: 'up' | 'down') => {
    const idx = festivals.findIndex(f => f.id === id);
    if (idx === -1) return;
    const swapIdx = direction === 'up' ? idx - 1 : idx + 1;
    if (swapIdx < 0 || swapIdx >= festivals.length) return;
    const a = festivals[idx];
    const b = festivals[swapIdx];
    await Promise.all([
      fetch(`/api/v1/admin/festivals/${a.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ sortOrder: b.sortOrder }) }),
      fetch(`/api/v1/admin/festivals/${b.id}`, { method: 'PUT', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ sortOrder: a.sortOrder }) }),
    ]);
    await fetchFestivals();
  };

  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">축제 관리</h1>
          <p className="text-sm text-gray-500 mt-1">홈 화면에 노출되는 축제 배너와 상세 정보를 관리합니다.</p>
        </div>
        <button onClick={openCreate} className="inline-flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-sm font-medium">
          <Plus className="w-4 h-4" /> 축제 등록
        </button>
      </div>

      {loading ? (
        <div className="text-center py-20 text-gray-500">불러오는 중...</div>
      ) : festivals.length === 0 ? (
        <div className="text-center py-20 text-gray-500">등록된 축제가 없습니다.</div>
      ) : (
        <div className="bg-white border border-gray-200 rounded-lg overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-700">
              <tr>
                <th className="px-4 py-3 text-left">순서</th>
                <th className="px-4 py-3 text-left">배너</th>
                <th className="px-4 py-3 text-left">축제명</th>
                <th className="px-4 py-3 text-left">기간</th>
                <th className="px-4 py-3 text-left">상태</th>
                <th className="px-4 py-3 text-left">노출</th>
                <th className="px-4 py-3 text-left">콘텐츠</th>
                <th className="px-4 py-3 text-right">관리</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {festivals.map((f, idx) => (
                <tr key={f.id}>
                  <td className="px-4 py-3 align-top">
                    <div className="flex items-center gap-1">
                      <button disabled={idx === 0} onClick={() => reorder(f.id, 'up')} className="p-1 disabled:opacity-30 hover:bg-gray-100 rounded"><ChevronUp className="w-4 h-4" /></button>
                      <button disabled={idx === festivals.length - 1} onClick={() => reorder(f.id, 'down')} className="p-1 disabled:opacity-30 hover:bg-gray-100 rounded"><ChevronDown className="w-4 h-4" /></button>
                      <span className="ml-1 text-gray-500">{f.sortOrder}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3 align-top">
                    {f.bannerUrl ? <img src={f.bannerUrl} alt="" className="w-20 h-12 object-cover rounded" /> : <div className="w-20 h-12 bg-gray-100 rounded flex items-center justify-center text-xs text-gray-400">-</div>}
                  </td>
                  <td className="px-4 py-3 align-top">
                    <div className="font-medium text-gray-900">{f.name}</div>
                    {f.address && <div className="text-xs text-gray-500 mt-0.5">{f.address}</div>}
                  </td>
                  <td className="px-4 py-3 align-top text-gray-700">
                    {f.startDate.slice(0, 10)} ~ {f.endDate.slice(0, 10)}
                  </td>
                  <td className="px-4 py-3 align-top">
                    <span className={`inline-block px-2 py-0.5 rounded text-xs ${STATUS_COLOR[f.status] ?? 'bg-gray-100 text-gray-800'}`}>{STATUS_MAP[f.status] ?? f.status}</span>
                  </td>
                  <td className="px-4 py-3 align-top">
                    <span className={`inline-block px-2 py-0.5 rounded text-xs ${f.isActive ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-500'}`}>{f.isActive ? '활성' : '비활성'}</span>
                  </td>
                  <td className="px-4 py-3 align-top text-xs text-gray-600">
                    {f._count && (
                      <div className="flex flex-wrap gap-x-3 gap-y-0.5">
                        <span>행사 {f._count.programs}</span>
                        <span>이벤트 {f._count.events}</span>
                        <span>미션 {f._count.missions}</span>
                        <span>스탬프 {f._count.stamps}</span>
                        <span>쿠폰 {f._count.coupons}</span>
                        <span>상점 {f._count.stores}</span>
                      </div>
                    )}
                  </td>
                  <td className="px-4 py-3 align-top text-right">
                    <div className="inline-flex gap-1">
                      <button onClick={() => openEdit(f)} className="p-1.5 text-gray-600 hover:bg-gray-100 rounded"><Edit className="w-4 h-4" /></button>
                      <button onClick={() => handleDelete(f.id, f.name)} className="p-1.5 text-red-600 hover:bg-red-50 rounded"><Trash2 className="w-4 h-4" /></button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showForm && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50">
          <form onSubmit={handleSubmit} className="bg-white rounded-lg w-full max-w-2xl max-h-[90vh] overflow-y-auto">
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-200">
              <h2 className="text-lg font-semibold text-gray-900">{editingId ? '축제 수정' : '축제 등록'}</h2>
              <button type="button" onClick={closeForm} className="p-1.5 hover:bg-gray-100 rounded"><X className="w-5 h-5" /></button>
            </div>
            <div className="p-6 space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">축제명 *</label>
                <input value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" required />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">설명</label>
                <textarea value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} rows={3} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">시작일 *</label>
                  <input type="date" value={form.startDate} onChange={e => setForm({ ...form, startDate: e.target.value })} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" required />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">종료일 *</label>
                  <input type="date" value={form.endDate} onChange={e => setForm({ ...form, endDate: e.target.value })} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" required />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">주소</label>
                <input value={form.address} onChange={e => setForm({ ...form, address: e.target.value })} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">위도 (지도보기 중심)</label>
                  <input value={form.latitude} onChange={e => setForm({ ...form, latitude: e.target.value })} placeholder="35.8425" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">경도</label>
                  <input value={form.longitude} onChange={e => setForm({ ...form, longitude: e.target.value })} placeholder="128.6905" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">배너 이미지 (홈 노출)</label>
                <div className="flex items-center gap-3">
                  {form.bannerUrl ? <img src={form.bannerUrl} alt="" className="w-32 h-20 object-cover rounded border" /> : <div className="w-32 h-20 bg-gray-100 rounded flex items-center justify-center text-xs text-gray-400">미등록</div>}
                  <input ref={bannerInputRef} type="file" accept="image/*" onChange={e => handleImageUpload('bannerUrl', e)} className="hidden" />
                  <button type="button" onClick={() => bannerInputRef.current?.click()} className="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50"><Upload className="w-4 h-4" /> 업로드</button>
                  {form.bannerUrl && <button type="button" onClick={() => setForm({ ...form, bannerUrl: '' })} className="text-sm text-red-600">제거</button>}
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">상세 이미지</label>
                <div className="flex items-center gap-3">
                  {form.imageUrl ? <img src={form.imageUrl} alt="" className="w-32 h-20 object-cover rounded border" /> : <div className="w-32 h-20 bg-gray-100 rounded flex items-center justify-center text-xs text-gray-400">미등록</div>}
                  <input ref={imageInputRef} type="file" accept="image/*" onChange={e => handleImageUpload('imageUrl', e)} className="hidden" />
                  <button type="button" onClick={() => imageInputRef.current?.click()} className="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm border border-gray-300 rounded-lg hover:bg-gray-50"><Upload className="w-4 h-4" /> 업로드</button>
                  {form.imageUrl && <button type="button" onClick={() => setForm({ ...form, imageUrl: '' })} className="text-sm text-red-600">제거</button>}
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">노출 순서 (작을수록 먼저)</label>
                  <input type="number" value={form.sortOrder} onChange={e => setForm({ ...form, sortOrder: e.target.value })} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
                </div>
                <div className="flex items-end">
                  <label className="inline-flex items-center gap-2 text-sm">
                    <input type="checkbox" checked={form.isActive} onChange={e => setForm({ ...form, isActive: e.target.checked })} className="w-4 h-4" />
                    홈 화면에 노출
                  </label>
                </div>
              </div>
            </div>
            <div className="px-6 py-4 border-t border-gray-200 flex justify-end gap-2">
              <button type="button" onClick={closeForm} className="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50">취소</button>
              <button type="submit" disabled={submitting} className="inline-flex items-center gap-2 px-4 py-2 text-sm bg-blue-600 hover:bg-blue-700 text-white rounded-lg disabled:opacity-50"><Save className="w-4 h-4" /> {submitting ? '저장 중...' : '저장'}</button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
