'use client';

import { Plus, Upload, Edit, Trash2, Filter, X, Save } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';

interface Program {
  id: number;
  festivalId: number;
  festival?: { id: number; name: string };
  name: string;
  description?: string;
  category: string;
  subcategory?: string;
  hasCoupon?: boolean;
  imageUrl?: string;
  operatingHours?: string;
  location?: string;
  phone?: string;
  latitude?: number;
  longitude?: number;
  speaker?: string;
  startDate: string;
  endDate: string;
  status: string;
  _count: { events: number };
}

interface FestivalOption {
  id: number;
  name: string;
}

const STATUS_MAP: Record<string, string> = {
  scheduled: '예정',
  in_progress: '진행중',
  ended: '종료',
};

const CATEGORY_MAP: Record<string, string> = {
  exhibition: '전시',
  seminar: '세미나',
  food: '맛집',
  experience: '체험',
};

const CATEGORY_COLOR: Record<string, string> = {
  exhibition: 'bg-purple-100 text-purple-800',
  seminar: 'bg-amber-100 text-amber-800',
  food: 'bg-orange-100 text-orange-800',
  experience: 'bg-emerald-100 text-emerald-800',
};

interface FormState {
  festivalId: string;
  name: string;
  description: string;
  category: string;
  subcategory: string;
  hasCoupon: boolean;
  imageUrl: string;
  operatingHours: string;
  location: string;
  phone: string;
  latitude: string;
  longitude: string;
  speaker: string;
  startDate: string;
  endDate: string;
}

const EMPTY_FORM: FormState = {
  festivalId: '',
  name: '',
  description: '',
  category: 'exhibition',
  subcategory: '',
  hasCoupon: false,
  imageUrl: '',
  operatingHours: '',
  location: '',
  phone: '',
  latitude: '',
  longitude: '',
  speaker: '',
  startDate: '',
  endDate: '',
};

export default function ProgramsPage() {
  const [programs, setPrograms] = useState<Program[]>([]);
  const [festivals, setFestivals] = useState<FestivalOption[]>([]);
  const [festivalFilter, setFestivalFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    fetch('/api/v1/admin/festivals')
      .then((res) => res.json())
      .then((data) => {
        if (data.success) {
          setFestivals(data.data.map((f: { id: number; name: string }) => ({ id: f.id, name: f.name })));
        }
      });
  }, []);

  const fetchPrograms = () => {
    const params = new URLSearchParams();
    if (statusFilter !== 'all') params.set('status', statusFilter);
    if (categoryFilter !== 'all') params.set('category', categoryFilter);
    if (festivalFilter !== 'all') params.set('festivalId', festivalFilter);
    fetch(`/api/v1/admin/programs?${params}`)
      .then((res) => res.json())
      .then((data) => { if (data.success) setPrograms(data.data); });
  };

  useEffect(() => { fetchPrograms(); }, [statusFilter, categoryFilter, festivalFilter]);

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
        setForm((prev) => ({ ...prev, imageUrl: json.data.imageUrl }));
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
    setForm({ ...EMPTY_FORM, festivalId: festivals[0]?.id ? String(festivals[0].id) : '' });
    if (fileInputRef.current) fileInputRef.current.value = '';
    setShowForm(true);
  };

  const openEditForm = (program: Program) => {
    setEditingId(program.id);
    setForm({
      festivalId: String(program.festivalId),
      name: program.name,
      description: program.description || '',
      category: program.category,
      subcategory: program.subcategory || '',
      hasCoupon: program.hasCoupon || false,
      imageUrl: program.imageUrl || '',
      operatingHours: program.operatingHours || '',
      location: program.location || '',
      phone: program.phone || '',
      latitude: program.latitude?.toString() || '',
      longitude: program.longitude?.toString() || '',
      speaker: program.speaker || '',
      startDate: program.startDate.split('T')[0],
      endDate: program.endDate.split('T')[0],
    });
    setShowForm(true);
  };

  const handleSubmit = async () => {
    if (!form.festivalId) {
      alert('축제를 선택하세요');
      return;
    }
    const isEdit = editingId !== null;
    const url = isEdit ? `/api/v1/admin/programs/${editingId}` : '/api/v1/admin/programs';
    const method = isEdit ? 'PUT' : 'POST';

    try {
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...form,
          festivalId: Number(form.festivalId),
          latitude: form.latitude ? parseFloat(form.latitude) : undefined,
          longitude: form.longitude ? parseFloat(form.longitude) : undefined,
        }),
      });
      const data = await res.json();
      if (data.success) {
        setShowForm(false);
        setForm(EMPTY_FORM);
        setEditingId(null);
        fetchPrograms();
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
      const res = await fetch(`/api/v1/admin/programs/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (data.success) {
        fetchPrograms();
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
          <h2 className="text-2xl font-semibold text-gray-900">행사 관리</h2>
          <p className="text-sm text-gray-500 mt-1">프로그램 행사를 등록하고 관리합니다</p>
        </div>
        <button
          onClick={openCreateForm}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Plus className="w-4 h-4" />
          행사 등록
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              {editingId ? '행사 수정' : '새 행사 등록'}
            </h3>
            <button onClick={() => { setShowForm(false); setEditingId(null); }} className="text-gray-400 hover:text-gray-600">
              <X className="w-5 h-5" />
            </button>
          </div>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">축제 *</label>
              <select value={form.festivalId} onChange={(e) => setForm({ ...form, festivalId: e.target.value })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                <option value="">축제를 선택하세요</option>
                {festivals.map(f => <option key={f.id} value={f.id}>{f.name}</option>)}
              </select>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">행사명 *</label>
                <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
                  placeholder="행사 이름을 입력하세요"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">카테고리 *</label>
                <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                  <option value="exhibition">전시</option>
                  <option value="seminar">세미나</option>
                  <option value="food">맛집</option>
                  <option value="experience">체험</option>
                </select>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">소분류</label>
                <input type="text" value={form.subcategory} onChange={(e) => setForm({ ...form, subcategory: e.target.value })}
                  placeholder="예: 카페, 베이커리, 레스토랑"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">쿠폰 사용 가능</label>
                <label className="flex items-center gap-3 h-[42px] cursor-pointer">
                  <input type="checkbox" checked={form.hasCoupon} onChange={(e) => setForm({ ...form, hasCoupon: e.target.checked })}
                    className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-2 focus:ring-blue-500" />
                  <span className="text-sm text-gray-700">이 행사에서 쿠폰을 제공합니다</span>
                </label>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">설명</label>
              <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
                placeholder="행사 설명을 입력하세요" rows={3}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">운영 시간</label>
                <input type="text" value={form.operatingHours} onChange={(e) => setForm({ ...form, operatingHours: e.target.value })}
                  placeholder="예: 10:00 - 18:00"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">장소</label>
                <input type="text" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })}
                  placeholder="예: 알파시티 2로 33 태왕알파시티 3층"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
            </div>
            <div className="grid grid-cols-3 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">연락처</label>
                <input type="text" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })}
                  placeholder="예: 051-123-4567"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">위도</label>
                <input type="number" step="0.0000001" value={form.latitude} onChange={(e) => setForm({ ...form, latitude: e.target.value })}
                  placeholder="예: 35.8420000"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">경도</label>
                <input type="number" step="0.0000001" value={form.longitude} onChange={(e) => setForm({ ...form, longitude: e.target.value })}
                  placeholder="예: 128.6900000"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
            </div>
            {form.category === 'seminar' && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">연사 정보</label>
                <input type="text" value={form.speaker} onChange={(e) => setForm({ ...form, speaker: e.target.value })}
                  placeholder="예: 김태현 교수(서울대학교 경제학과)"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
            )}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">행사 이미지</label>
              <div className="flex items-center gap-4">
                <label className="border-2 border-dashed border-gray-300 rounded-lg p-4 text-center hover:border-blue-400 transition-colors cursor-pointer inline-block">
                  <Upload className="w-6 h-6 text-gray-400 mx-auto mb-1" />
                  <p className="text-sm text-gray-600">이미지 업로드</p>
                  <p className="text-xs text-gray-500 mt-0.5">PNG, JPG (최대 5MB)</p>
                  <input ref={fileInputRef} type="file" accept="image/*" onChange={handleUpload} className="hidden" />
                </label>
                {uploading && <span className="text-sm text-gray-500">업로드 중...</span>}
              </div>
              {form.imageUrl && (
                <div className="mt-3 flex items-center gap-3">
                  <img src={form.imageUrl} alt="미리보기" className="h-20 w-20 rounded-lg object-cover border border-gray-200" />
                  <button onClick={() => { setForm({ ...form, imageUrl: '' }); if (fileInputRef.current) fileInputRef.current.value = ''; }}
                    className="text-sm text-red-500 hover:text-red-700">삭제</button>
                </div>
              )}
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">시작일 *</label>
                <input type="date" value={form.startDate} onChange={(e) => setForm({ ...form, startDate: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">종료일 *</label>
                <input type="date" value={form.endDate} onChange={(e) => setForm({ ...form, endDate: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
            </div>
            <button onClick={handleSubmit}
              className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
              <Save className="w-4 h-4" />
              {editingId ? '수정' : '등록'}
            </button>
          </div>
        </div>
      )}

      {/* 필터 */}
      <div className="mb-6 bg-white rounded-lg border border-gray-200 p-4">
        <div className="flex items-center gap-2 mb-3">
          <Filter className="w-4 h-4 text-gray-500" />
          <span className="text-sm font-medium text-gray-700">필터</span>
        </div>
        <div className="flex gap-6 flex-wrap">
          <div>
            <span className="text-xs text-gray-500 mb-1 block">축제</span>
            <select value={festivalFilter} onChange={(e) => setFestivalFilter(e.target.value)}
              className="px-3 py-2 text-sm border border-gray-300 rounded-lg bg-white">
              <option value="all">전체</option>
              {festivals.map(f => <option key={f.id} value={f.id}>{f.name}</option>)}
            </select>
          </div>
          <div>
            <span className="text-xs text-gray-500 mb-1 block">상태</span>
            <div className="flex gap-2">
              {['all', 'scheduled', 'in_progress', 'ended'].map((status) => (
                <button key={status} onClick={() => setStatusFilter(status)}
                  className={`px-4 py-2 text-sm rounded-lg transition-colors ${
                    statusFilter === status ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}>
                  {status === 'all' ? '전체' : STATUS_MAP[status]}
                </button>
              ))}
            </div>
          </div>
          <div>
            <span className="text-xs text-gray-500 mb-1 block">카테고리</span>
            <div className="flex gap-2">
              {['all', 'exhibition', 'seminar', 'food', 'experience'].map((cat) => (
                <button key={cat} onClick={() => setCategoryFilter(cat)}
                  className={`px-4 py-2 text-sm rounded-lg transition-colors ${
                    categoryFilter === cat ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                  }`}>
                  {cat === 'all' ? '전체' : CATEGORY_MAP[cat]}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* 테이블 */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">행사명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">카테고리</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">기간</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">운영시간</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">이벤트</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {programs.map((program) => (
              <tr key={program.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div className="text-sm font-medium text-gray-900">{program.name}</div>
                  {program.location && (
                    <div className="text-xs text-gray-500 mt-1">{program.location}</div>
                  )}
                </td>
                <td className="px-6 py-4">
                  <span className={`inline-flex px-2 py-1 text-xs rounded-full ${CATEGORY_COLOR[program.category] || 'bg-gray-100 text-gray-800'}`}>
                    {CATEGORY_MAP[program.category] || program.category}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {new Date(program.startDate).toLocaleDateString()} ~ {new Date(program.endDate).toLocaleDateString()}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">{program.operatingHours || '-'}</td>
                <td className="px-6 py-4">
                  <span className={`inline-flex px-2 py-1 text-xs rounded-full ${
                    program.status === 'in_progress' ? 'bg-green-100 text-green-800'
                    : program.status === 'scheduled' ? 'bg-blue-100 text-blue-800'
                    : 'bg-gray-100 text-gray-800'
                  }`}>
                    {STATUS_MAP[program.status] || program.status}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">{program._count.events}개</td>
                <td className="px-6 py-4">
                  <div className="flex gap-2">
                    <button onClick={() => openEditForm(program)}
                      className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                      <Edit className="w-4 h-4" />
                    </button>
                    <button onClick={() => handleDelete(program.id)}
                      className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {programs.length === 0 && (
              <tr><td colSpan={7} className="px-6 py-8 text-center text-sm text-gray-500">등록된 행사가 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
