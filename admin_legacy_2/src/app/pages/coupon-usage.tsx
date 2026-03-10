import { CheckCircle, X } from "lucide-react";

export function CouponUsage() {
  const usageRequests = [
    {
      id: 1,
      userName: "김철수",
      couponName: "커피 무료 쿠폰",
      code: "FEST-2025-A1B2",
      requestTime: "2025-01-29 14:32",
      status: "대기중",
      storeName: "카페 ABC",
    },
    {
      id: 2,
      userName: "이영희",
      couponName: "기념품 교환권",
      code: "FEST-2025-C3D4",
      requestTime: "2025-01-29 14:28",
      status: "대기중",
      storeName: "기념품샵",
    },
    {
      id: 3,
      userName: "박민수",
      couponName: "10% 할인 쿠폰",
      code: "FEST-2025-E5F6",
      requestTime: "2025-01-29 14:15",
      status: "완료",
      storeName: "푸드코트",
    },
  ];

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-900">쿠폰 사용 확인</h2>
        <p className="text-sm text-gray-500 mt-1">
          사용자의 쿠폰 사용 요청을 확인하고 처리합니다
        </p>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                사용자
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                쿠폰
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                쿠폰 코드
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                사용처
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                요청 시간
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                관리
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {usageRequests.map((request) => (
              <tr key={request.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-900">
                  {request.userName}
                </td>
                <td className="px-6 py-4 text-sm text-gray-900">
                  {request.couponName}
                </td>
                <td className="px-6 py-4">
                  <code className="px-2 py-1 text-xs bg-gray-100 text-gray-700 rounded font-mono">
                    {request.code}
                  </code>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {request.storeName}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {request.requestTime}
                </td>
                <td className="px-6 py-4">
                  <span
                    className={`inline-flex px-2 py-1 text-xs rounded-full ${
                      request.status === "완료"
                        ? "bg-green-100 text-green-800"
                        : "bg-yellow-100 text-yellow-800"
                    }`}
                  >
                    {request.status}
                  </span>
                </td>
                <td className="px-6 py-4">
                  {request.status === "대기중" ? (
                    <div className="flex gap-2">
                      <button className="flex items-center gap-1 px-3 py-1.5 text-sm bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors">
                        <CheckCircle className="w-4 h-4" />
                        사용 완료
                      </button>
                      <button className="p-1.5 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                        <X className="w-4 h-4" />
                      </button>
                    </div>
                  ) : (
                    <span className="text-sm text-gray-500">처리 완료</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 통계 요약 */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-6">
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">오늘 사용 요청</p>
          <p className="text-3xl font-semibold text-gray-900">25</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">처리 대기중</p>
          <p className="text-3xl font-semibold text-yellow-600">8</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">처리 완료</p>
          <p className="text-3xl font-semibold text-green-600">17</p>
        </div>
      </div>
    </div>
  );
}
