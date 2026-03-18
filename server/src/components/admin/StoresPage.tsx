'use client';

import { CheckCircle, X as XIcon, MapPin, Clock, Store, Ticket, Link } from 'lucide-react';
import { useEffect, useState } from 'react';

interface StoreCouponItem {
  coupon: { id: number; name: string };
}

interface StoreItem {
  id: number;
  name: string;
  category: string;
  ownerName: string;
  phone: string;
  address?: string;
  addressDetail?: string;
  description?: string;
  imageUrl?: string;
  storeCode?: string;
  operatingDays?: string;
  openTime?: string;
  closeTime?: string;
  programId?: number | null;
  program?: { id: number; name: string } | null;
  missionId?: number | null;
  mission?: { id: number; name: string; type: string } | null;
  storeCoupons?: StoreCouponItem[];
  requestDate: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}

interface ProgramOption {
  id: number;
  name: string;
}

interface MissionOption {
  id: number;
  name: string;
  type: string;
}

interface CouponOption {
  id: number;
  name: string;
}

interface Stats {
  totalCount: number;
  pendingCount: number;
  approvedCount: number;
  rejectedCount: number;
}

const CATEGORY_MAP: Record<string, string> = {
  cafe: '카페',
  restaurant: '음식점',
  shopping: '쇼핑',
  hotel: '호텔',
  convenience: '편의시설',
};
const STATUS_MAP: Record<string, string> = { pending: '대기중', approved: '승인', rejected: '반려' };

export default function StoresPage() {
  const [stores, setStores] = useState<StoreItem[]>([]);
  const [stats, setStats] = useState<Stats>({ totalCount: 0, pendingCount: 0, approvedCount: 0, rejectedCount: 0 });
  const [selected, setSelected] = useState<StoreItem | null>(null);
  const [programs, setPrograms] = useState<ProgramOption[]>([]);
  const [missions, setMissions] = useState<MissionOption[]>([]);
  const [coupons, setCoupons] = useState<CouponOption[]>([]);
  const [editProgramId, setEditProgramId] = useState<number | null>(null);
  const [editMissionId, setEditMissionId] = useState<number | null>(null);
  const [editCouponIds, setEditCouponIds] = useState<number[]>([]);
  const [saving, setSaving] = useState(false);

  const fetchData = () => {
    fetch('/api/v1/admin/stores').then((r) => r.json()).then((d) => {
      if (d.success) { setStores(d.data.stores); setStats(d.data.stats); }
    });
  };

  const fetchOptions = () => {
    fetch('/api/v1/admin/programs').then((r) => r.json()).then((d) => {
      if (d.success) setPrograms(d.data.map((p: ProgramOption) => ({ id: p.id, name: p.name })));
    });
    fetch('/api/v1/admin/missions').then((r) => r.json()).then((d) => {
      if (d.success) setMissions(d.data.map((m: MissionOption) => ({ id: m.id, name: m.name, type: m.type })));
    });
    fetch('/api/v1/admin/coupons').then((r) => r.json()).then((d) => {
      if (d.success) setCoupons(d.data.map((c: CouponOption) => ({ id: c.id, name: c.name })));
    });
  };

  useEffect(() => { fetchData(); fetchOptions(); }, []);

  const openDetail = (store: StoreItem) => {
    setSelected(store);
    setEditProgramId(store.programId ?? null);
    setEditMissionId(store.missionId ?? null);
    setEditCouponIds(store.storeCoupons?.map(sc => sc.coupon.id) ?? []);
  };

  const handleAction = async (id: number, status: 'approved' | 'rejected') => {
    try {
      const res = await fetch(`/api/v1/admin/stores/${id}`, {
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

  const handleSaveLinks = async () => {
    if (!selected) return;
    setSaving(true);
    try {
      const res = await fetch(`/api/v1/admin/stores/${selected.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ programId: editProgramId, missionId: editMissionId, couponIds: editCouponIds }),
      });
      const data = await res.json();
      if (data.success) {
        fetchData();
        setSelected(data.data);
        alert('저장되었습니다');
      } else {
        alert(`저장 실패: ${data.error?.message || '알 수 없는 오류'}`);
      }
    } catch {
      alert('저장 실패: 서버와 통신할 수 없습니다');
    } finally {
      setSaving(false);
    }
  };

  const toggleCoupon = (couponId: number) => {
    setEditCouponIds(prev =>
      prev.includes(couponId) ? prev.filter(id => id !== couponId) : [...prev, couponId]
    );
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
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">연결 미션</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">연결 쿠폰</th>
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
                <td className="px-6 py-4 text-sm text-gray-600">
                  {store.mission ? (
                    <span className="inline-flex px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-800">{store.mission.name}</span>
                  ) : (
                    <span className="text-gray-400">미연결</span>
                  )}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {store.storeCoupons && store.storeCoupons.length > 0 ? (
                    <div className="flex flex-wrap gap-1">
                      {store.storeCoupons.map(sc => (
                        <span key={sc.coupon.id} className="inline-flex px-2 py-0.5 text-xs rounded-full bg-orange-100 text-orange-800">{sc.coupon.name}</span>
                      ))}
                    </div>
                  ) : (
                    <span className="text-gray-400">없음</span>
                  )}
                </td>
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
                  <div className="flex gap-2">
                    {store.status === 'pending' && (
                      <>
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
                      </>
                    )}
                    <button onClick={() => openDetail(store)} className="text-sm text-blue-600 hover:underline">상세보기</button>
                  </div>
                </td>
              </tr>
            ))}
            {stores.length === 0 && (
              <tr><td colSpan={6} className="px-6 py-8 text-center text-sm text-gray-500">등록된 상점이 없습니다</td></tr>
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
          <div className="bg-white rounded-lg w-full max-w-lg mx-4 max-h-[80vh] overflow-y-auto" onClick={(e) => e.stopPropagation()}>
            <div className="sticky top-0 bg-white p-6 pb-4 border-b border-gray-100">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold text-gray-900">상점 상세정보</h3>
                <button onClick={() => setSelected(null)} className="p-1 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100 transition-colors">
                  <XIcon className="w-5 h-5" />
                </button>
              </div>
            </div>
            <div className="p-6 pt-4 space-y-4">
              {selected.imageUrl && (
                <div>
                  <img src={selected.imageUrl} alt={selected.name} className="w-full h-40 object-cover rounded-lg" />
                </div>
              )}
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
              {(selected.address || selected.addressDetail) && (
                <div>
                  <p className="text-xs text-gray-500 mb-1 flex items-center gap-1"><MapPin className="w-3 h-3" /> 주소</p>
                  <p className="text-sm text-gray-900">{selected.address}{selected.addressDetail ? ` ${selected.addressDetail}` : ''}</p>
                </div>
              )}
              {selected.storeCode && (
                <div>
                  <p className="text-xs text-gray-500 mb-1 flex items-center gap-1"><Store className="w-3 h-3" /> 상점 코드</p>
                  <p className="text-sm font-mono text-gray-900">{selected.storeCode}</p>
                </div>
              )}
              {(selected.operatingDays || selected.openTime) && (
                <div>
                  <p className="text-xs text-gray-500 mb-1 flex items-center gap-1"><Clock className="w-3 h-3" /> 운영 정보</p>
                  <p className="text-sm text-gray-900">
                    {selected.operatingDays && `${selected.operatingDays} `}
                    {selected.openTime && selected.closeTime && `${selected.openTime} ~ ${selected.closeTime}`}
                  </p>
                </div>
              )}
              {selected.description && (
                <div>
                  <p className="text-xs text-gray-500 mb-1">상점 설명</p>
                  <p className="text-sm text-gray-900 whitespace-pre-line">{selected.description}</p>
                </div>
              )}
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

              {/* 미션 연결 */}
              <div className="border-t border-gray-200 pt-4">
                <p className="text-sm font-semibold text-gray-900 mb-2 flex items-center gap-1"><Link className="w-4 h-4" /> 미션 연결</p>
                <select
                  value={editMissionId ?? ''}
                  onChange={(e) => setEditMissionId(e.target.value ? parseInt(e.target.value) : null)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">미션 선택 (미연결)</option>
                  {missions.map(m => (
                    <option key={m.id} value={m.id}>{m.name} ({m.type === 'quiz' ? '퀴즈' : m.type === 'location_auth' ? '위치인증' : '체류시간'})</option>
                  ))}
                </select>
              </div>

              {/* 쿠폰 연결 */}
              <div className="border-t border-gray-200 pt-4">
                <p className="text-sm font-semibold text-gray-900 mb-2 flex items-center gap-1"><Ticket className="w-4 h-4" /> 사용 가능 쿠폰</p>
                {coupons.length === 0 ? (
                  <p className="text-sm text-gray-400">등록된 쿠폰이 없습니다</p>
                ) : (
                  <div className="space-y-2">
                    {coupons.map(coupon => (
                      <label key={coupon.id} className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={editCouponIds.includes(coupon.id)}
                          onChange={() => toggleCoupon(coupon.id)}
                          className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                        />
                        <span className="text-sm text-gray-900">{coupon.name}</span>
                      </label>
                    ))}
                  </div>
                )}
              </div>

              {/* 저장 버튼 */}
              <button
                onClick={handleSaveLinks}
                disabled={saving}
                className="w-full py-2.5 text-sm font-medium bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                {saving ? '저장 중...' : '미션/쿠폰 연결 저장'}
              </button>
            </div>
            <div className="sticky bottom-0 bg-white flex justify-end gap-2 p-6 pt-4 border-t border-gray-200">
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
