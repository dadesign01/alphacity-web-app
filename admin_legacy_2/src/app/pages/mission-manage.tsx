import { Plus } from "lucide-react";
import { useState } from "react";

export function MissionManage() {
  const [missionType, setMissionType] = useState("quiz");
  const [stayTime, setStayTime] = useState("5");

  const missions = [
    { id: 1, name: "포토존 인증", type: "위치 인증", location: "포토존 1" },
    { id: 2, name: "프로그램 퀴즈", type: "퀴즈", question: "프로그램 시작 연도는?" },
    {
      id: 3,
      name: "푸드코트 체류",
      type: "체류시간",
      location: "푸드 코트",
      time: "5분",
    },
  ];

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">미션 관리</h2>
          <p className="text-sm text-gray-500 mt-1">사용자 미션을 생성하고 관리합니다</p>
        </div>
      </div>

      {/* 미션 등록 폼 */}
      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">새 미션 등록</h3>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              미션 유형 선택
            </label>
            <div className="flex gap-3">
              {[
                { value: "quiz", label: "퀴즈" },
                { value: "location", label: "위치 인증" },
                { value: "stay", label: "체류시간" },
              ].map((type) => (
                <button
                  key={type.value}
                  onClick={() => setMissionType(type.value)}
                  className={`px-4 py-2 text-sm rounded-lg transition-colors ${
                    missionType === type.value
                      ? "bg-blue-600 text-white"
                      : "bg-gray-100 text-gray-700 hover:bg-gray-200"
                  }`}
                >
                  {type.label}
                </button>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              미션명
            </label>
            <input
              type="text"
              placeholder="미션 이름을 입력하세요"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          {missionType === "quiz" && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                퀴즈 질문
              </label>
              <input
                type="text"
                placeholder="퀴즈 질문을 입력하세요"
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              />
            </div>
          )}

          {missionType === "location" && (
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                인증 장소
              </label>
              <select className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                <option>메인 게이트</option>
                <option>푸드 코트</option>
                <option>공연장</option>
                <option>포토존 1</option>
              </select>
            </div>
          )}

          {missionType === "stay" && (
            <>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  체류 장소
                </label>
                <select className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
                  <option>메인 게이트</option>
                  <option>푸드 코트</option>
                  <option>공연장</option>
                  <option>포토존 1</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  인증 시간 설정
                </label>
                <div className="flex items-center gap-3">
                  <input
                    type="number"
                    value={stayTime}
                    onChange={(e) => setStayTime(e.target.value)}
                    min="1"
                    max="60"
                    className="w-24 px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  />
                  <span className="text-sm text-gray-600">분</span>
                </div>
                <p className="text-xs text-gray-500 mt-1">
                  사용자가 해당 장소에 머물러야 하는 최소 시간
                </p>
              </div>
            </>
          )}

          <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
            <Plus className="w-4 h-4" />
            미션 등록
          </button>
        </div>
      </div>

      {/* 등록된 미션 목록 */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">등록된 미션</h3>
        </div>
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                미션명
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                유형
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상세
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                관리
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {missions.map((mission) => (
              <tr key={mission.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-900">{mission.name}</td>
                <td className="px-6 py-4">
                  <span className="inline-flex px-2 py-1 text-xs rounded-full bg-purple-100 text-purple-800">
                    {mission.type}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {"location" in mission && mission.location}
                  {"question" in mission && mission.question}
                  {"time" in mission && `${mission.location} / ${mission.time}`}
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