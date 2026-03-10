import { Plus, Edit, Trash2, Filter } from "lucide-react";
import { useState } from "react";

export function Events() {
  const [statusFilter, setStatusFilter] = useState("all");

  const events = [
    {
      id: 1,
      name: "2025 봄꽃 프로그램",
      date: "2025-04-01 ~ 2025-04-07",
      status: "진행중",
      participants: 1247,
    },
    {
      id: 2,
      name: "여름 음악 페스티벌",
      date: "2025-07-15 ~ 2025-07-17",
      status: "예정",
      participants: 0,
    },
    {
      id: 3,
      name: "가을 단풍 프로그램",
      date: "2024-10-10 ~ 2024-10-20",
      status: "종료",
      participants: 3542,
    },
  ];

  const filteredEvents =
    statusFilter === "all"
      ? events
      : events.filter((e) => e.status === statusFilter);

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">행사 관리</h2>
          <p className="text-sm text-gray-500 mt-1">프로그램 행사를 등록하고 관리합니다</p>
        </div>
        <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          <Plus className="w-4 h-4" />
          행사 등록
        </button>
      </div>

      {/* 필터 */}
      <div className="mb-6 bg-white rounded-lg border border-gray-200 p-4">
        <div className="flex items-center gap-2 mb-3">
          <Filter className="w-4 h-4 text-gray-500" />
          <span className="text-sm font-medium text-gray-700">상태 필터</span>
        </div>
        <div className="flex gap-2">
          {["all", "예정", "진행중", "종료"].map((status) => (
            <button
              key={status}
              onClick={() => setStatusFilter(status)}
              className={`px-4 py-2 text-sm rounded-lg transition-colors ${
                statusFilter === status
                  ? "bg-blue-600 text-white"
                  : "bg-gray-100 text-gray-700 hover:bg-gray-200"
              }`}
            >
              {status === "all" ? "전체" : status}
            </button>
          ))}
        </div>
      </div>

      {/* 테이블 */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                행사명
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                기간
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                참여자
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                관리
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {filteredEvents.map((event) => (
              <tr key={event.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm text-gray-900">{event.name}</td>
                <td className="px-6 py-4 text-sm text-gray-600">{event.date}</td>
                <td className="px-6 py-4">
                  <span
                    className={`inline-flex px-2 py-1 text-xs rounded-full ${
                      event.status === "진행중"
                        ? "bg-green-100 text-green-800"
                        : event.status === "예정"
                        ? "bg-blue-100 text-blue-800"
                        : "bg-gray-100 text-gray-800"
                    }`}
                  >
                    {event.status}
                  </span>
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {event.participants.toLocaleString()}명
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