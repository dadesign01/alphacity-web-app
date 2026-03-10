import { Users, Calendar, Stamp, Ticket } from "lucide-react";

export function Dashboard() {
  const stats = [
    {
      label: "오늘 진행중인 행사 수",
      value: "3",
      icon: Calendar,
      color: "bg-blue-500",
    },
    {
      label: "참여 사용자 수",
      value: "1,247",
      icon: Users,
      color: "bg-green-500",
    },
    {
      label: "발급된 스탬프 수",
      value: "4,582",
      icon: Stamp,
      color: "bg-purple-500",
    },
    {
      label: "사용된 쿠폰 수",
      value: "892",
      icon: Ticket,
      color: "bg-orange-500",
    },
  ];

  return (
    <div className="p-8">
      <div className="mb-8">
        <h2 className="text-2xl font-semibold text-gray-900">대시보드</h2>
        <p className="text-sm text-gray-500 mt-1">
          오늘의 주요 지표를 확인하세요
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <div
              key={stat.label}
              className="bg-white rounded-lg border border-gray-200 p-6"
            >
              <div className="flex items-center justify-between mb-4">
                <div className={`${stat.color} p-3 rounded-lg`}>
                  <Icon className="w-6 h-6 text-white" />
                </div>
              </div>
              <p className="text-sm text-gray-600 mb-1">{stat.label}</p>
              <p className="text-3xl font-semibold text-gray-900">{stat.value}</p>
            </div>
          );
        })}
      </div>

      <div className="mt-8 bg-white rounded-lg border border-gray-200 p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">
          최근 활동 현황
        </h3>
        <div className="space-y-3">
          {[
            { time: "15분 전", content: "새로운 사용자 3명이 가입했습니다" },
            { time: "1시간 전", content: "쿠폰 15개가 사용되었습니다" },
            { time: "2시간 전", content: "스탬프 미션 완료 알림 25건" },
            { time: "3시간 전", content: "새로운 상점 등록 신청 2건" },
          ].map((activity, idx) => (
            <div
              key={idx}
              className="flex items-start gap-3 pb-3 border-b border-gray-100 last:border-0"
            >
              <div className="w-2 h-2 rounded-full bg-blue-500 mt-2" />
              <div className="flex-1">
                <p className="text-sm text-gray-900">{activity.content}</p>
                <p className="text-xs text-gray-500 mt-1">{activity.time}</p>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
