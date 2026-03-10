import { Plus, Send } from "lucide-react";

export function NotificationManage() {
  const notifications = [
    {
      id: 1,
      title: "새로운 이벤트 시작!",
      message: "스탬프 랠리 이벤트가 시작되었습니다.",
      sentDate: "2025-01-29 10:00",
      recipients: 1247,
    },
    {
      id: 2,
      title: "프로그램 일정 안내",
      message: "오늘 공연 일정을 확인하세요.",
      sentDate: "2025-01-28 15:00",
      recipients: 1150,
    },
    {
      id: 3,
      title: "쿠폰 사용 마감 임박",
      message: "3일 후 쿠폰 유효기간이 종료됩니다.",
      sentDate: "2025-01-27 12:00",
      recipients: 892,
    },
  ];

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-900">알림 관리</h2>
        <p className="text-sm text-gray-500 mt-1">
          사용자에게 푸시 알림을 전송합니다
        </p>
      </div>

      {/* 알림 전송 폼 */}
      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">새 알림 전송</h3>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              알림 제목
            </label>
            <input
              type="text"
              placeholder="알림 제목을 입력하세요"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              알림 내용
            </label>
            <textarea
              placeholder="알림 내용을 입력하세요"
              rows={4}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              수신 대상
            </label>
            <select className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent">
              <option>전체 사용자</option>
              <option>활성 사용자만</option>
              <option>특정 그룹</option>
            </select>
          </div>

          <div className="flex gap-3">
            <button className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
              <Send className="w-4 h-4" />
              즉시 전송
            </button>
            <button className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors">
              예약 전송
            </button>
          </div>
        </div>
      </div>

      {/* 전송 내역 */}
      <div className="bg-white rounded-lg border border-gray-200 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200">
          <h3 className="text-lg font-medium text-gray-900">전송 내역</h3>
        </div>
        <table className="w-full">
          <thead className="bg-gray-50 border-b border-gray-200">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                제목
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                내용
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                전송일시
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                수신자 수
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {notifications.map((notification) => (
              <tr key={notification.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 text-sm font-medium text-gray-900">
                  {notification.title}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {notification.message}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {notification.sentDate}
                </td>
                <td className="px-6 py-4 text-sm text-gray-600">
                  {notification.recipients.toLocaleString()}명
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}