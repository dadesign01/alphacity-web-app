import { Plus, Edit, Trash2 } from "lucide-react";

export function EventsManage() {
  const events = [
    {
      id: 1,
      name: "스탬프 10개 달성 추첨 이벤트",
      type: "추첨",
      startDate: "2025-04-01",
      endDate: "2025-04-07",
      reward: "애플워치",
      participantLimit: 100,
      currentParticipants: 67,
    },
    {
      id: 2,
      name: "선착순 100명 경품 이벤트",
      type: "선착순",
      startDate: "2025-04-01",
      endDate: "2025-04-07",
      reward: "텀블러",
      participantLimit: 100,
      currentParticipants: 100,
    },
    {
      id: 3,
      name: "포토존 인증 추첨 이벤트",
      type: "추첨",
      startDate: "2025-04-05",
      endDate: "2025-04-10",
      reward: "기프티콘 1만원",
      participantLimit: 200,
      currentParticipants: 123,
    },
  ];

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">추첨/선착순 이벤트 관리</h2>
          <p className="text-sm text-gray-500 mt-1">프로그램 내 추첨 및 선착순 이벤트를 관리합니다</p>
        </div>
        <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus className="w-4 h-4" />
          이벤트 등록
        </button>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                이벤트명
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                이벤트 기간
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                보상
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                참여 제한 수
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                관리
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {events.map((event) => (
              <tr key={event.id} className="hover:bg-gray-50">
                <td className="px-6 py-4">
                  <div className="flex flex-col gap-1">
                    <span className="text-sm font-medium text-gray-900">{event.name}</span>
                    <span className={`inline-flex w-fit px-2 py-0.5 text-xs rounded-full ${
                      event.type === "추첨" 
                        ? "bg-purple-100 text-purple-800" 
                        : "bg-green-100 text-green-800"
                    }`}>
                      {event.type}
                    </span>
                  </div>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {event.startDate} ~ {event.endDate}
                </td>
                <td className="px-6 py-4 text-sm font-medium text-gray-900">{event.reward}</td>
                <td className="px-6 py-4">
                  <div className="flex flex-col gap-1">
                    <span className="text-sm text-gray-900">
                      {event.currentParticipants} / {event.participantLimit}명
                    </span>
                    <div className="w-full bg-gray-200 rounded-full h-1.5">
                      <div
                        className={`h-1.5 rounded-full ${
                          event.currentParticipants >= event.participantLimit
                            ? "bg-red-500"
                            : "bg-blue-600"
                        }`}
                        style={{
                          width: `${Math.min(
                            (event.currentParticipants / event.participantLimit) * 100,
                            100
                          )}%`,
                        }}
                      ></div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4">
                  <div className="flex gap-2">
                    <button className="p-2 text-blue-600 hover:bg-blue-50 rounded-lg transition-colors">
                      <Edit className="w-4 h-4" />
                    </button>
                    <button className="p-2 text-red-600 hover:bg-red-50 rounded-lg transition-colors">
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}