'use client';

import { Search } from 'lucide-react';
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

export default function UsersPage() {
  const [users, setUsers] = useState<UserItem[]>([]);
  const [stats, setStats] = useState<Stats>({ totalUsers: 0, todayUsers: 0, activeUsers: 0 });
  const [search, setSearch] = useState('');

  const fetchData = (q?: string) => {
    const url = q ? `/api/v1/admin/users?search=${encodeURIComponent(q)}` : '/api/v1/admin/users';
    fetch(url).then((r) => r.json()).then((d) => {
      if (d.success) { setUsers(d.data.users); setStats(d.data.stats); }
    });
  };

  useEffect(() => { fetchData(); }, []);

  const handleSearch = () => { fetchData(search); };

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
                  <button className="text-sm text-blue-600 hover:underline">상세보기</button>
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
    </div>
  );
}
