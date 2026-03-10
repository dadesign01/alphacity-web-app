import { Search } from "lucide-react";

export function UserManage() {
  const users = [
    {
      id: 1,
      name: "김철수",
      email: "kim@example.com",
      loginType: "이메일",
      joinDate: "2025-01-15",
      stampCount: 5,
      couponCount: 2,
      lastVisit: "2025-01-29",
    },
    {
      id: 2,
      name: "이영희",
      email: "lee@example.com",
      loginType: "소셜 (카카오)",
      joinDate: "2025-01-20",
      stampCount: 3,
      couponCount: 1,
      lastVisit: "2025-01-28",
    },
    {
      id: 3,
      name: "박민수",
      email: "park@example.com",
      loginType: "소셜 (구글)",
      joinDate: "2025-01-10",
      stampCount: 8,
      couponCount: 3,
      lastVisit: "2025-01-29",
    },
  ];

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-900">사용자 관리</h2>
        <p className="text-sm text-gray-500 mt-1">
          가입한 사용자의 정보와 활동 내역을 확인합니다
        </p>
      </div>

      {/* 검색 */}
      <div className="bg-white rounded-lg border border-gray-200 p-4 mb-6">
        <div className="flex gap-3">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              placeholder="이름 또는 이메일로 검색"
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
            검색
          </button>
        </div>
      </div>

      {/* 사용자 목록 */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                사용자
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                로그인 유형
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                가입일
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                스탬프
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                쿠폰
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                마지막 방문
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                관리
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {users.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {user.name}
                    </p>
                    <p className="text-xs text-gray-500">{user.email}</p>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <span className="inline-flex px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-800">
                    {user.loginType}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {user.joinDate}
                </td>
                <td className="px-6 py-4 text-sm text-gray-900">
                  {user.stampCount}개
                </td>
                <td className="px-6 py-4 text-sm text-gray-900">
                  {user.couponCount}개
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {user.lastVisit}
                </td>
                <td className="px-6 py-4">
                  <button className="text-sm text-blue-600 hover:underline">
                    상세보기
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 통계 */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mt-6">
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">전체 사용자</p>
          <p className="text-3xl font-semibold text-gray-900">1,247</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">오늘 가입</p>
          <p className="text-3xl font-semibold text-blue-600">23</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">활성 사용자</p>
          <p className="text-3xl font-semibold text-green-600">892</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">이메일 인증율</p>
          <p className="text-3xl font-semibold text-purple-600">87%</p>
        </div>
      </div>
    </div>
  );
}
