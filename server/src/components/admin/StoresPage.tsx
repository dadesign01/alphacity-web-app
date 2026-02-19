'use client';

import { CheckCircle, X as XIcon } from 'lucide-react';
import { useEffect, useState } from 'react';

interface StoreItem {
  id: number;
  name: string;
  category: string;
  ownerName: string;
  phone: string;
  requestDate: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

interface Stats {
  totalCount: number;
  pendingCount: number;
  approvedCount: number;
  rejectedCount: number;
}

const CATEGORY_MAP: Record<string, string> = { food: '식음료', souvenir: '기념품', other: '기타' };
const STATUS_MAP: Record<string, string> = { pending: '대기중', approved: '승인', rejected: '반려' };

export default function StoresPage() {
  const [stores, setStores] = useState<StoreItem[]>([]);
  const [stats, setStats] = useState<Stats>({ totalCount: 0, pendingCount: 0, approvedCount: 0, rejectedCount: 0 });
  const [selected, setSelected] = useState<StoreItem | null>(null);

  const fetchData = () => {
    fetch('/api/v1/admin/stores').then((r) => r.json()).then((d) => {
      if (d.success) { setStores(d.data.stores); setStats(d.data.stats); }
    });
  };

  useEffect(() => { fetchData(); }, []);

  const handleAction = async (id: number, status: 'approved' | 'rejected') => {
    try {
      const res = await fetch(`/api/v1/admin/stores/${id}/status`, {
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
        <h2 className="text-2xl font-semibold text-gray-900">상점 관리</h2>
        <p className="text-sm text-gray-500 mt-1">상점 등록 신청을 검토하고 승인/반려합니다</p>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상점명</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">카테고리</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">운영자</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">연락처</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">신청일</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {stores.map((store) => (
              <tr key={store.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm font-medium text-gray-900">{store.name}</td>
                <td className="px-6 py-4">
                  <span className="inline-flex px-2 py-1 text-xs rounded-full bg-purple-100 text-purple-800">
                    {CATEGORY_MAP[store.category] || store.category}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-900">{store.ownerName}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{store.phone}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{new Date(store.requestDate).toLocaleDateString()}</td>
                <td className="px-6 py-4">
                  <span className={`inline-flex px-2 py-1 text-xs rounded-full ${
                    store.status === 'approved' ? 'bg-green-100 text-green-800'
                    : store.status === 'rejected' ? 'bg-red-100 text-red-800'
                    : 'bg-yellow-100 text-yellow-800'
                  }`}>
                    {STATUS_MAP[store.status] || store.status}
                  </span>
                </td>
                <td className="px-6 py-4">
                  {store.status === 'pending' ? (
                    <div className="flex gap-2">
                      <button onClick={() => handleAction(store.id, 'approved')}
                        className="flex items-center gap-1 px-3 py-1.5 text-sm bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors">
                        <CheckCircle className="w-4 h-4" />
                        승인
                      </button>
                      <button onClick={() => handleAction(store.id, 'rejected')}
                        className="flex items-center gap-1 px-3 py-1.5 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">
                        <XIcon className="w-4 h-4" />
                        반려
                      </button>
                    </div>
                  ) : (
                    <button onClick={() => setSelected(store)} className="text-sm text-blue-600 hover:underline">상세보기</button>
                  )}
                </td>
              </tr>
            ))}
            {stores.length === 0 && (
              <tr><td colSpan={7} className="px-6 py-8 text-center text-sm text-gray-500">등록된 상점이 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mt-6">
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">전체 상점</p>
          <p className="text-3xl font-semibold text-gray-900">{stats.totalCount}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">승인 대기</p>
          <p className="text-3xl font-semibold text-yellow-600">{stats.pendingCount}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">승인 완료</p>
          <p className="text-3xl font-semibold text-green-600">{stats.approvedCount}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">반려</p>
          <p className="text-3xl font-semibold text-red-600">{stats.rejectedCount}</p>
        </div>
      </div>

      {selected && (
        <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50" onClick={() => setSelected(null)}>
          <div className="bg-white rounded-lg w-full max-w-lg mx-4 p-6" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-gray-900">상점 상세정보</h3>
              <button onClick={() => setSelected(null)} className="p-1 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition-colors">
                <XIcon className="w-5 h-5" />
              </button>
            </div>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-xs text-gray-500 mb-1">상점명</p>
                  <p className="text-sm font-medium text-gray-900">{selected.name}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 mb-1">카테고리</p>
                  <span className="inline-flex px-2 py-1 text-xs rounded-full bg-purple-100 text-purple-800">
                    {CATEGORY_MAP[selected.category] || selected.category}
                  </span>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-xs text-gray-500 mb-1">운영자</p>
                  <p className="text-sm text-gray-900">{selected.ownerName}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 mb-1">연락처</p>
                  <p className="text-sm text-gray-900">{selected.phone}</p>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-xs text-gray-500 mb-1">신청일</p>
                  <p className="text-sm text-gray-900">{new Date(selected.requestDate).toLocaleDateString()}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 mb-1">상태</p>
                  <span className={`inline-flex px-2 py-1 text-xs rounded-full ${
                    selected.status === 'approved' ? 'bg-green-100 text-green-800'
                    : selected.status === 'rejected' ? 'bg-red-100 text-red-800'
                    : 'bg-yellow-100 text-yellow-800'
                  }`}>
                    {STATUS_MAP[selected.status] || selected.status}
                  </span>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-xs text-gray-500 mb-1">등록일</p>
                  <p className="text-sm text-gray-900">{new Date(selected.createdAt).toLocaleString()}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-500 mb-1">최근 수정일</p>
                  <p className="text-sm text-gray-900">{new Date(selected.updatedAt).toLocaleString()}</p>
                </div>
              </div>
            </div>
            <div className="flex justify-end gap-2 mt-6 pt-4 border-t border-gray-200">
              {selected.status !== 'approved' && (
                <button onClick={() => { handleAction(selected.id, 'approved'); setSelected(null); }}
                  className="flex items-center gap-1 px-4 py-2 text-sm bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors">
                  <CheckCircle className="w-4 h-4" />
                  승인
                </button>
              )}
              {selected.status !== 'rejected' && (
                <button onClick={() => { handleAction(selected.id, 'rejected'); setSelected(null); }}
                  className="flex items-center gap-1 px-4 py-2 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">
                  <XIcon className="w-4 h-4" />
                  반려
                </button>
              )}
              <button onClick={() => setSelected(null)}
                className="px-4 py-2 text-sm border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors">
                닫기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
