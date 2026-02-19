'use client';

import { Plus, Upload } from 'lucide-react';
import { useEffect, useState } from 'react';

interface CouponItem {
  id: number;
  name: string;
  requiredStamps: number;
  validUntil: string;
  description: string | null;
  _count: { userCoupons: number };
}

export default function CouponsPage() {
  const [coupons, setCoupons] = useState<CouponItem[]>([]);
  const [form, setForm] = useState({ name: '', requiredStamps: 0, validUntil: '', description: '' });

  const fetchCoupons = () => {
    fetch('/api/v1/admin/coupons').then((r) => r.json()).then((d) => { if (d.success) setCoupons(d.data); });
  };

  useEffect(() => { fetchCoupons(); }, []);

  const handleCreate = async () => {
    try {
      const res = await fetch('/api/v1/admin/coupons', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(form),
      });
      const data = await res.json();
      if (data.success) {
        setForm({ name: '', requiredStamps: 0, validUntil: '', description: '' });
        fetchCoupons();
      } else {
        alert(`쿠폰 등록 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('쿠폰 등록 실패: 서버와 통신할 수 없습니다');
    }
  };

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">쿠폰 관리</h2>
          <p className="text-sm text-gray-500 mt-1">쿠폰을 등록하고 관리합니다</p>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">새 쿠폰 등록</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">쿠폰명</label>
            <input type="text" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
              placeholder="쿠폰 이름을 입력하세요"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
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
            <div className="border-2 border-dashed border-gray-300 rounded-lg p-6 text-center hover:border-blue-400 transition-colors cursor-pointer">
              <Upload className="w-8 h-8 text-gray-400 mx-auto mb-2" />
              <p className="text-sm text-gray-600">클릭하여 이미지 업로드</p>
              <p className="text-xs text-gray-500 mt-1">PNG, JPG (최대 5MB)</p>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">쿠폰 설명</label>
            <textarea value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}
              placeholder="쿠폰 사용 방법 및 상세 설명을 입력하세요" rows={4}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
          </div>
          <button onClick={handleCreate}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
            <Plus className="w-4 h-4" />
            쿠폰 등록
          </button>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">등록된 쿠폰</h3>
        </div>
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">쿠폰명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">필요 스탬프</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">유효기간</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">발급 수</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {coupons.map((coupon) => (
              <tr key={coupon.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-900">{coupon.name}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{coupon.requiredStamps}개</td>
                <td className="px-6 py-4 text-sm text-gray-600">{new Date(coupon.validUntil).toLocaleDateString()}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{coupon._count.userCoupons}개</td>
                <td className="px-6 py-4">
                  <button className="text-sm text-blue-600 hover:underline">수정</button>
                </td>
              </tr>
            ))}
            {coupons.length === 0 && (
              <tr><td colSpan={5} className="px-6 py-8 text-center text-sm text-gray-500">등록된 쿠폰이 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
