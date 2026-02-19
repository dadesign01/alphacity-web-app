'use client';

import { CheckCircle, X } from 'lucide-react';
import { useEffect, useState } from 'react';

interface UsageRequest {
  id: number;
  code: string;
  status: string;
  requestedAt: string | null;
  usedAt: string | null;
  user: { nickname: string; email: string };
  coupon: { name: string };
  store: { name: string } | null;
}

interface Stats {
  todayRequests: number;
  pendingCount: number;
  completedCount: number;
}

const STATUS_MAP: Record<string, string> = { pending: '대기중', used: '완료', rejected: '반려', issued: '발급' };

export default function CouponUsagePage() {
  const [requests, setRequests] = useState<UsageRequest[]>([]);
  const [stats, setStats] = useState<Stats>({ todayRequests: 0, pendingCount: 0, completedCount: 0 });

  const fetchData = () => {
    fetch('/api/v1/admin/coupons/usage').then((r) => r.json()).then((d) => {
      if (d.success) { setRequests(d.data.requests); setStats(d.data.stats); }
    });
  };

  useEffect(() => { fetchData(); }, []);

  const handleAction = async (id: number, status: 'used' | 'rejected') => {
    try {
      const res = await fetch(`/api/v1/admin/coupons/usage/${id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status }),
      });
      const data = await res.json();
      if (data.success) {
        fetchData();
      } else {
        alert(`처리 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('처리 실패: 서버와 통신할 수 없습니다');
    }
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-900">쿠폰 사용 확인</h2>
        <p className="text-sm text-gray-500 mt-1">사용자의 쿠폰 사용 요청을 확인하고 처리합니다</p>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">사용자</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">쿠폰</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">쿠폰 코드</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">사용처</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">요청 시간</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {requests.map((req) => (
              <tr key={req.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-900">{req.user.nickname}</td>
                <td className="px-6 py-4 text-sm text-gray-900">{req.coupon.name}</td>
                <td className="px-6 py-4">
                  <code className="px-2 py-1 text-xs bg-gray-100 text-gray-700 rounded font-mono">{req.code}</code>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">{req.store?.name || '-'}</td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {req.requestedAt ? new Date(req.requestedAt).toLocaleString() : '-'}
                </td>
                <td className="px-6 py-4">
                  <span className={`inline-flex px-2 py-1 text-xs rounded-full ${
                    req.status === 'used' ? 'bg-green-100 text-green-800'
                    : req.status === 'rejected' ? 'bg-red-100 text-red-800'
                    : 'bg-yellow-100 text-yellow-800'
                  }`}>
                    {STATUS_MAP[req.status] || req.status}
                  </span>
                </td>
                <td className="px-6 py-4">
                  {req.status === 'pending' ? (
                    <div className="flex gap-2">
                      <button onClick={() => handleAction(req.id, 'used')}
                        className="flex items-center gap-1 px-3 py-1.5 text-sm bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors">
                        <CheckCircle className="w-4 h-4" />
                        사용 완료
                      </button>
                      <button onClick={() => handleAction(req.id, 'rejected')}
                        className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                        <X className="w-4 h-4" />
                      </button>
                    </div>
                  ) : (
                    <span className="text-sm text-gray-500">처리 완료</span>
                  )}
                </td>
              </tr>
            ))}
            {requests.length === 0 && (
              <tr><td colSpan={7} className="px-6 py-8 text-center text-sm text-gray-500">사용 요청이 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-6">
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">오늘 사용 요청</p>
          <p className="text-3xl font-semibold text-gray-900">{stats.todayRequests}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">처리 대기중</p>
          <p className="text-3xl font-semibold text-yellow-600">{stats.pendingCount}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">처리 완료</p>
          <p className="text-3xl font-semibold text-green-600">{stats.completedCount}</p>
        </div>
      </div>
    </div>
  );
}
