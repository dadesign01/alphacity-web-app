import { Plus } from "lucide-react";

export function StampManage() {
  const stamps = [
    { id: 1, name: "첫 방문 스탬프", condition: "프로그램 첫 방문", count: 1247 },
    { id: 2, name: "미션 완료 스탬프", condition: "미션 3개 완료", count: 892 },
    { id: 3, name: "이벤트 참여 스탬프", condition: "이벤트 1회 참여", count: 654 },
  ];

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">스탬프 관리</h2>
          <p className="text-sm text-gray-500 mt-1">스탬프 발급 조건을 설정합니다</p>
        </div>
        <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus className="w-4 h-4" />
          스탬프 추가
        </button>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">스탬프 등록</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              스탬프명
            </label>
            <input
              type="text"
              placeholder="스탬프 이름을 입력하세요"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              발급 조건 설정
            </label>
            <select className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
              <option>미션 완료</option>
              <option>이벤트 참여</option>
              <option>장소 방문</option>
              <option>퀴즈 정답</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              상세 조건
            </label>
            <textarea
              placeholder="발급 조건에 대한 상세 설명을 입력하세요"
              rows={3}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
            등록
          </button>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">등록된 스탬프</h3>
        </div>
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                스탬프명
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                발급 조건
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                발급 수
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                관리
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {stamps.map((stamp) => (
              <tr key={stamp.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-900">{stamp.name}</td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {stamp.condition}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {stamp.count.toLocaleString()}개
                </td>
                <td className="px-6 py-4">
                  <button className="text-sm text-blue-600 hover:underline">
                    수정
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}