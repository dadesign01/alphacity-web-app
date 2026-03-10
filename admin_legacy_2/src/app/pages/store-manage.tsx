import { CheckCircle, X } from "lucide-react";

export function StoreManage() {
  const stores = [
    {
      id: 1,
      name: "카페 ABC",
      category: "식음료",
      owner: "김사장",
      phone: "010-1234-5678",
      requestDate: "2025-01-25",
      status: "대기중",
    },
    {
      id: 2,
      name: "기념품샵",
      category: "기념품",
      owner: "이사장",
      phone: "010-2345-6789",
      requestDate: "2025-01-26",
      status: "대기중",
    },
    {
      id: 3,
      name: "푸드코트",
      category: "식음료",
      owner: "박사장",
      phone: "010-3456-7890",
      requestDate: "2025-01-20",
      status: "승인",
    },
    {
      id: 4,
      name: "액세서리 가게",
      category: "기타",
      owner: "최사장",
      phone: "010-4567-8901",
      requestDate: "2025-01-18",
      status: "반려",
    },
  ];

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-900">상점 관리</h2>
        <p className="text-sm text-gray-500 mt-1">
          상점 등록 신청을 검토하고 승인/반려합니다
        </p>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상점명
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                카테고리
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                운영자
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                연락처
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                신청일
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
            {stores.map((store) => (
              <tr key={store.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm font-medium text-gray-900">
                  {store.name}
                </td>
                <td className="px-6 py-4">
                  <span className="inline-flex px-2 py-1 text-xs rounded-full bg-purple-100 text-purple-800">
                    {store.category}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-900">{store.owner}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{store.phone}</td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {store.requestDate}
                </td>
                <td className="px-6 py-4">
                  <span
                    className={`inline-flex px-2 py-1 text-xs rounded-full ${
                      store.status === "승인"
                        ? "bg-green-100 text-green-800"
                        : store.status === "반려"
                        ? "bg-red-100 text-red-800"
                        : "bg-yellow-100 text-yellow-800"
                    }`}
                  >
                    {store.status}
                  </span>
                </td>
                <td className="px-6 py-4">
                  {store.status === "대기중" ? (
                    <div className="flex gap-2">
                      <button className="flex items-center gap-1 px-3 py-1.5 text-sm bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors">
                        <CheckCircle className="w-4 h-4" />
                        승인
                      </button>
                      <button className="flex items-center gap-1 px-3 py-1.5 text-sm bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors">
                        <X className="w-4 h-4" />
                        반려
                      </button>
                    </div>
                  ) : (
                    <button className="text-sm text-blue-600 hover:underline">
                      상세보기
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 통계 */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mt-6">
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">전체 상점</p>
          <p className="text-3xl font-semibold text-gray-900">24</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">승인 대기</p>
          <p className="text-3xl font-semibold text-yellow-600">2</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">승인 완료</p>
          <p className="text-3xl font-semibold text-green-600">20</p>
        </div>
        <div className="bg-white rounded-lg border border-gray-200 p-6">
          <p className="text-sm text-gray-600 mb-1">반려</p>
          <p className="text-3xl font-semibold text-red-600">2</p>
        </div>
      </div>
    </div>
  );
}
