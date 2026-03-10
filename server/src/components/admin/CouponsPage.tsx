'use client';

import { Plus, Upload, Edit, Trash2, X, Save } from 'lucide-react';
import { useEffect, useRef, useState } from 'react';

interface CouponItem {
  id: number;
  name: string;
  requiredStamps: number;
  validUntil: string;
  description: string | null;
  imageUrl: string | null;
  program: { id: number; name: string } | null;
  _count: { userCoupons: number };
}

interface OptionItem { id: number; name: string; }

const EMPTY_FORM = { name: '', requiredStamps: 0, validUntil: '', description: '', programId: 0 };

export default function CouponsPage() {
  const [coupons, setCoupons] = useState<CouponItem[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [programs, setPrograms] = useState<OptionItem[]>([]);
  const [imageUrl, setImageUrl] = useState('');
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const fetchCoupons = () => {
    fetch('/api/v1/admin/coupons').then((r) => r.json()).then((d) => { if (d.success) setCoupons(d.data); });
  };

  useEffect(() => {
    fetchCoupons();
    fetch('/api/v1/admin/programs').then((r) => r.json()).then((d) => {
      if (d.success) setPrograms(d.data.map((p: OptionItem) => ({ id: p.id, name: p.name })));
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

  const openEditForm = (coupon: CouponItem) => {
    setEditingId(coupon.id);
    setForm({
      name: coupon.name,
      requiredStamps: coupon.requiredStamps,
      validUntil: coupon.validUntil.split('T')[0],
      description: coupon.description || '',
      programId: coupon.program?.id || 0,
    });
    setImageUrl(coupon.imageUrl || '');
    setShowForm(true);
  };

  const handleSubmit = async () => {
    const isEdit = editingId !== null;
    const url = isEdit ? `/api/v1/admin/coupons/${editingId}` : '/api/v1/admin/coupons';
    const method = isEdit ? 'PUT' : 'POST';

    try {
      const res = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...form,
          programId: form.programId || null,
          imageUrl: imageUrl || null,
        }),
      });
      const data = await res.json();
      if (data.success) {
        setShowForm(false);
        setEditingId(null);
        setForm(EMPTY_FORM);
        setImageUrl('');
        fetchCoupons();
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
      const res = await fetch(`/api/v1/admin/coupons/${id}`, { method: 'DELETE' });
      const data = await res.json();
      if (data.success) {
        fetchCoupons();
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
          <h2 className="text-2xl font-semibold text-gray-900">쿠폰 관리</h2>
          <p className="text-sm text-gray-500 mt-1">쿠폰을 등록하고 관리합니다</p>
        </div>
        <button onClick={openCreateForm}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus className="w-4 h-4" />
          쿠폰 등록
        </button>
      </div>

      {showForm && (
        <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-medium text-gray-900">
              {editingId ? '쿠폰 수정' : '새 쿠폰 등록'}
            </h3>
            <button onClick={() => { setShowForm(false); setEditingId(null); }} className="text-gray-400 hover:text-gray-600">
              <X className="w-5 h-5" />
            </button>
          </div>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">쿠폰명</label>
              <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
                placeholder="쿠폰 이름을 입력하세요"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">연결된 프로그램</label>
              <select value={form.programId} onChange={(e) => setForm({ ...form, programId: Number(e.target.value) })}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                <option value={0}>선택 안 함</option>
                {programs.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">필요 스탬프 수</label>
                <input type="number" value={form.requiredStamps} onChange={(e) => setForm({ ...form, requiredStamps: Number(e.target.value) })}
                  placeholder="0" min="0"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">유효기간</label>
                <input type="date" value={form.validUntil} onChange={(e) => setForm({ ...form, validUntil: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">쿠폰 이미지</label>
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
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">쿠폰 설명</label>
              <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
                placeholder="쿠폰 사용 방법 및 상세 설명을 입력하세요" rows={4}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
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
          <h3 className="text-lg font-medium text-gray-900">등록된 쿠폰</h3>
        </div>
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">쿠폰명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">프로그램</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">필요 스탬프</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">유효기간</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">발급 수</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {coupons.map((coupon) => (
              <tr key={coupon.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-900">
                  <div className="flex items-center gap-3">
                    {coupon.imageUrl && (
                      <img src={coupon.imageUrl} alt={coupon.name} className="h-8 w-8 rounded object-cover" />
                    )}
                    {coupon.name}
                  </div>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">{coupon.program?.name || '-'}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{coupon.requiredStamps}개</td>
                <td className="px-6 py-4 text-sm text-gray-600">{new Date(coupon.validUntil).toLocaleDateString()}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{coupon._count.userCoupons}개</td>
                <td className="px-6 py-4">
                  <div className="flex gap-2">
                    <button onClick={() => openEditForm(coupon)}
                      className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                      <Edit className="w-4 h-4" />
                    </button>
                    <button onClick={() => handleDelete(coupon.id)}
                      className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
            {coupons.length === 0 && (
              <tr><td colSpan={6} className="px-6 py-8 text-center text-sm text-gray-500">등록된 쿠폰이 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
