'use client';

import { Search, X, Stamp, Ticket, Target, CalendarCheck, User } from 'lucide-react';
import { useEffect, useState } from 'react';

interface UserItem {
  id: number;
  nickname: string;
  email: string;
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
  _count: { userStamps: number; userCoupons: number };
}

interface Stats {
  totalUsers: number;
  todayUsers: number;
  activeUsers: number;
}

interface UserDetail {
  id: number;
  nickname: string;
  name: string | null;
  email: string;
  phone: string | null;
  address: string | null;
  addressDetail: string | null;
  birthDate: string | null;
  gender: string | null;
  profileImage: string | null;
  isActive: boolean;
  createdAt: string;
  userStamps: { id: number; collectedAt: string; stamp: { id: number; name: string; imageUrl: string | null; conditionType: string } }[];
  userCoupons: { id: number; code: string; status: string; createdAt: string; coupon: { id: number; name: string; imageUrl: string | null } }[];
  missionCompletions: { id: number; completedAt: string; mission: { id: number; name: string; type: string } }[];
  eventParticipants: { id: number; joinedAt: string; event: { id: number; name: string; type: string } }[];
}

const GENDER_LABEL: Record<string, string> = { male: '남성', female: '여성', other: '기타' };
const COUPON_STATUS: Record<string, string> = { issued: '발급됨', pending: '사용 대기', used: '사용 완료', rejected: '반려' };
const COUPON_STATUS_COLOR: Record<string, string> = {
  issued: 'bg-blue-100 text-blue-800', pending: 'bg-yellow-100 text-yellow-800',
  used: 'bg-green-100 text-green-800', rejected: 'bg-red-100 text-red-800',
};
const MISSION_TYPE: Record<string, string> = { quiz: '퀴즈', location_auth: '위치 인증', stay_time: '체류시간' };
const CONDITION_TYPE: Record<string, string> = { mission_complete: '미션 완료', event_participate: '이벤트 참여', place_visit: '장소 방문', quiz_correct: '퀴즈 정답' };

export default function UsersPage() {
  const [users, setUsers] = useState<UserItem[]>([]);
  const [stats, setStats] = useState<Stats>({ totalUsers: 0, todayUsers: 0, activeUsers: 0 });
  const [search, setSearch] = useState('');
  const [selectedUser, setSelectedUser] = useState<UserDetail | null>(null);
  const [detailLoading, setDetailLoading] = useState(false);

  const fetchData = (q?: string) => {
    const url = q ? `/api/v1/admin/users?search=${encodeURIComponent(q)}` : '/api/v1/admin/users';
    fetch(url).then((r) => r.json()).then((d) => {
      if (d.success) { setUsers(d.data.users); setStats(d.data.stats); }
    });
  };

  useEffect(() => { fetchData(); }, []);

  const handleSearch = () => { fetchData(search); };

  const openDetail = async (userId: number) => {
    setDetailLoading(true);
    try {
      const res = await fetch(`/api/v1/admin/users/${userId}`);
      const data = await res.json();
      if (data.success) setSelectedUser(data.data);
    } catch {
      alert('사용자 정보를 불러오지 못했습니다');
    }
    setDetailLoading(false);
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-900">사용자 관리</h2>
        <p className="text-sm text-gray-500 mt-1">가입한 사용자의 정보와 활동 내역을 확인합니다</p>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 p-4 mb-6">
        <div className="flex gap-3">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input type="text" value={search} onChange={(e) => setSearch(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              placeholder="이름 또는 이메일로 검색"
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent" />
          </div>
          <button onClick={handleSearch}
            className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">검색</button>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">사용자</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">가입일</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">스탬프</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">쿠폰</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {users.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div>
                    <p className="text-sm font-medium text-gray-900">{user.nickname}</p>
                    <p className="text-xs text-gray-500">{user.email}</p>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className={`inline-flex px-2 py-1 text-xs rounded-full ${
                    user.isActive ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                  }`}>
                    {user.isActive ? '활성' : '비활성'}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">{new Date(user.createdAt).toLocaleDateString()}</td>
                <td className="px-6 py-4 text-sm text-gray-900">{user._count.userStamps}개</td>
                <td className="px-6 py-4 text-sm text-gray-900">{user._count.userCoupons}개</td>
                <td className="px-6 py-4">
                  <button onClick={() => openDetail(user.id)}
                    className="text-sm text-blue-600 hover:underline">상세보기</button>
                </td>
              </tr>
            ))}
            {users.length === 0 && (
              <tr><td colSpan={6} className="px-6 py-8 text-center text-sm text-gray-500">등록된 사용자가 없습니다</td></tr>
            )}
          </tbody>
        </table>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-6">
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">전체 사용자</p>
          <p className="text-3xl font-semibold text-gray-900">{stats.totalUsers.toLocaleString()}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">오늘 가입</p>
          <p className="text-3xl font-semibold text-blue-600">{stats.todayUsers}</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">활성 사용자</p>
          <p className="text-3xl font-semibold text-green-600">{stats.activeUsers.toLocaleString()}</p>
        </div>
      </div>

      {/* Detail Modal */}
      {(selectedUser || detailLoading) && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40" onClick={() => !detailLoading && setSelectedUser(null)}>
          <div className="bg-white rounded-xl w-full max-w-2xl max-h-[85vh] overflow-y-auto mx-4" onClick={e => e.stopPropagation()}>
            {detailLoading ? (
              <div className="flex items-center justify-center py-20">
                <p className="text-sm text-gray-500">불러오는 중...</p>
              </div>
            ) : selectedUser && (
              <>
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-gray-200">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-semibold text-lg">
                      {selectedUser.nickname.charAt(0)}
                    </div>
                    <div>
                      <h3 className="text-lg font-semibold text-gray-900">
                        {selectedUser.name || selectedUser.nickname}
                        {selectedUser.name && <span className="text-sm font-normal text-gray-500 ml-2">({selectedUser.nickname})</span>}
                      </h3>
                      <p className="text-sm text-gray-500">{selectedUser.email}</p>
                    </div>
                  </div>
                  <button onClick={() => setSelectedUser(null)} className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100">
                    <X className="w-5 h-5" />
                  </button>
                </div>

                {/* Profile Info */}
                <div className="px-6 py-4 border-b border-gray-200">
                  <div className="flex items-center gap-2 mb-3">
                    <User className="w-4 h-4 text-gray-600" />
                    <h4 className="text-sm font-semibold text-gray-900">프로필 정보</h4>
                  </div>
                  <div className="grid grid-cols-2 gap-x-6 gap-y-3">
                    <div>
                      <p className="text-xs text-gray-500">이름</p>
                      <p className="text-sm font-medium text-gray-900 mt-0.5">{selectedUser.name || '-'}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">닉네임</p>
                      <p className="text-sm font-medium text-gray-900 mt-0.5">{selectedUser.nickname}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">이메일</p>
                      <p className="text-sm font-medium text-gray-900 mt-0.5">{selectedUser.email}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">전화번호</p>
                      <p className="text-sm font-medium text-gray-900 mt-0.5">{selectedUser.phone || '-'}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">주소</p>
                      <p className="text-sm font-medium text-gray-900 mt-0.5">
                        {selectedUser.address ? `${selectedUser.address}${selectedUser.addressDetail ? ` ${selectedUser.addressDetail}` : ''}` : '-'}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">생년월일</p>
                      <p className="text-sm font-medium text-gray-900 mt-0.5">
                        {selectedUser.birthDate ? new Date(selectedUser.birthDate).toLocaleDateString() : '-'}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">성별</p>
                      <p className="text-sm font-medium text-gray-900 mt-0.5">
                        {selectedUser.gender ? (GENDER_LABEL[selectedUser.gender] || selectedUser.gender) : '-'}
                      </p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">상태</p>
                      <span className={`inline-flex mt-0.5 px-2 py-0.5 text-xs rounded-full ${selectedUser.isActive ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'}`}>
                        {selectedUser.isActive ? '활성' : '비활성'}
                      </span>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">가입일</p>
                      <p className="text-sm font-medium text-gray-900 mt-0.5">{new Date(selectedUser.createdAt).toLocaleDateString()}</p>
                    </div>
                    <div>
                      <p className="text-xs text-gray-500">활동 요약</p>
                      <p className="text-sm font-medium text-gray-900 mt-0.5">
                        스탬프 {selectedUser.userStamps.length} · 쿠폰 {selectedUser.userCoupons.length} · 미션 {selectedUser.missionCompletions.length}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="p-6 space-y-6">
                  {/* Stamps */}
                  <section>
                    <div className="flex items-center gap-2 mb-3">
                      <Stamp className="w-4 h-4 text-amber-600" />
                      <h4 className="text-sm font-semibold text-gray-900">수집 스탬프 ({selectedUser.userStamps.length})</h4>
                    </div>
                    {selectedUser.userStamps.length > 0 ? (
                      <div className="space-y-2">
                        {selectedUser.userStamps.map(us => (
                          <div key={us.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                            <div className="flex items-center gap-3">
                              {us.stamp.imageUrl ? (
                                <img src={us.stamp.imageUrl} alt={us.stamp.name} className="w-8 h-8 rounded object-cover" />
                              ) : (
                                <div className="w-8 h-8 rounded bg-amber-100 flex items-center justify-center"><Stamp className="w-4 h-4 text-amber-600" /></div>
                              )}
                              <div>
                                <p className="text-sm font-medium text-gray-900">{us.stamp.name}</p>
                                <p className="text-xs text-gray-500">{CONDITION_TYPE[us.stamp.conditionType] || us.stamp.conditionType}</p>
                              </div>
                            </div>
                            <p className="text-xs text-gray-500">{new Date(us.collectedAt).toLocaleDateString()}</p>
                          </div>
                        ))}
                      </div>
                    ) : <p className="text-sm text-gray-400">수집한 스탬프가 없습니다</p>}
                  </section>

                  {/* Coupons */}
                  <section>
                    <div className="flex items-center gap-2 mb-3">
                      <Ticket className="w-4 h-4 text-blue-600" />
                      <h4 className="text-sm font-semibold text-gray-900">보유 쿠폰 ({selectedUser.userCoupons.length})</h4>
                    </div>
                    {selectedUser.userCoupons.length > 0 ? (
                      <div className="space-y-2">
                        {selectedUser.userCoupons.map(uc => (
                          <div key={uc.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                            <div>
                              <p className="text-sm font-medium text-gray-900">{uc.coupon.name}</p>
                              <p className="text-xs text-gray-500 font-mono">{uc.code}</p>
                            </div>
                            <span className={`inline-flex px-2 py-0.5 text-xs rounded-full ${COUPON_STATUS_COLOR[uc.status] || 'bg-gray-100 text-gray-800'}`}>
                              {COUPON_STATUS[uc.status] || uc.status}
                            </span>
                          </div>
                        ))}
                      </div>
                    ) : <p className="text-sm text-gray-400">보유한 쿠폰이 없습니다</p>}
                  </section>

                  {/* Missions */}
                  <section>
                    <div className="flex items-center gap-2 mb-3">
                      <Target className="w-4 h-4 text-purple-600" />
                      <h4 className="text-sm font-semibold text-gray-900">완료 미션 ({selectedUser.missionCompletions.length})</h4>
                    </div>
                    {selectedUser.missionCompletions.length > 0 ? (
                      <div className="space-y-2">
                        {selectedUser.missionCompletions.map(mc => (
                          <div key={mc.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                            <div>
                              <p className="text-sm font-medium text-gray-900">{mc.mission.name}</p>
                              <span className="text-xs px-2 py-0.5 bg-purple-100 text-purple-800 rounded">{MISSION_TYPE[mc.mission.type] || mc.mission.type}</span>
                            </div>
                            <p className="text-xs text-gray-500">{new Date(mc.completedAt).toLocaleDateString()}</p>
                          </div>
                        ))}
                      </div>
                    ) : <p className="text-sm text-gray-400">완료한 미션이 없습니다</p>}
                  </section>

                  {/* Events */}
                  <section>
                    <div className="flex items-center gap-2 mb-3">
                      <CalendarCheck className="w-4 h-4 text-green-600" />
                      <h4 className="text-sm font-semibold text-gray-900">참여 이벤트 ({selectedUser.eventParticipants.length})</h4>
                    </div>
                    {selectedUser.eventParticipants.length > 0 ? (
                      <div className="space-y-2">
                        {selectedUser.eventParticipants.map(ep => (
                          <div key={ep.id} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                            <div>
                              <p className="text-sm font-medium text-gray-900">{ep.event.name}</p>
                              <span className="text-xs px-2 py-0.5 bg-green-100 text-green-800 rounded">{ep.event.type === 'raffle' ? '추첨' : '선착순'}</span>
                            </div>
                            <p className="text-xs text-gray-500">{new Date(ep.joinedAt).toLocaleDateString()}</p>
                          </div>
                        ))}
                      </div>
                    ) : <p className="text-sm text-gray-400">참여한 이벤트가 없습니다</p>}
                  </section>
                </div>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
