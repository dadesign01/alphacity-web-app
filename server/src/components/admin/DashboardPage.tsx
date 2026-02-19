'use client';

import { Users, Calendar, Stamp, Ticket } from 'lucide-react';
import { useEffect, useState } from 'react';

interface Activity {
  type: string;
  content: string;
  time: string;
}

interface DashboardData {
  activePrograms: number;
  totalUsers: number;
  totalStamps: number;
  usedCoupons: number;
  recentActivities: Activity[];
}

function timeAgo(dateStr: string): string {
  const now = Date.now();
  const diff = now - new Date(dateStr).getTime();
  const minutes = Math.floor(diff / 60000);
  if (minutes < 1) return '방금 전';
  if (minutes < 60) return `${minutes}분 전`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}시간 전`;
  const days = Math.floor(hours / 24);
  return `${days}일 전`;
}

const TYPE_COLORS: Record<string, string> = {
  user: 'bg-green-500',
  coupon: 'bg-orange-500',
  mission: 'bg-purple-500',
  store: 'bg-blue-500',
  notification: 'bg-yellow-500',
};

export default function DashboardPage() {
  const [data, setData] = useState<DashboardData | null>(null);

  useEffect(() => {
    fetch('/api/v1/admin/dashboard')
      .then((res) => res.json())
      .then((d) => {
        if (d.success) setData(d.data);
      });
  }, []);

  const statCards = [
    {
      label: '오늘 진행중인 행사 수',
      value: data?.activePrograms ?? 0,
      icon: Calendar,
      color: 'bg-blue-500',
    },
    {
      label: '참여 사용자 수',
      value: data?.totalUsers ?? 0,
      icon: Users,
      color: 'bg-green-500',
    },
    {
      label: '발급된 스탬프 수',
      value: data?.totalStamps ?? 0,
      icon: Stamp,
      color: 'bg-purple-500',
    },
    {
      label: '사용된 쿠폰 수',
      value: data?.usedCoupons ?? 0,
      icon: Ticket,
      color: 'bg-orange-500',
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
        {statCards.map((stat) => {
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
              <p className="text-3xl font-semibold text-gray-900">
                {stat.value.toLocaleString()}
              </p>
            </div>
          );
        })}
      </div>

      <div className="mt-8 bg-white rounded-lg border border-gray-200 p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">
          최근 활동 현황
        </h3>
        <div className="space-y-3">
          {(data?.recentActivities ?? []).length > 0 ? (
            data!.recentActivities.map((activity, idx) => (
              <div
                key={idx}
                className="flex items-start gap-3 pb-3 border-b border-gray-100 last:border-0"
              >
                <div className={`w-2 h-2 rounded-full mt-2 ${TYPE_COLORS[activity.type] || 'bg-blue-500'}`} />
                <div className="flex-1">
                  <p className="text-sm text-gray-900">{activity.content}</p>
                  <p className="text-xs text-gray-500 mt-1">{timeAgo(activity.time)}</p>
                </div>
              </div>
            ))
          ) : (
            <p className="text-sm text-gray-500 text-center py-4">최근 활동이 없습니다</p>
          )}
        </div>
      </div>
    </div>
  );
}
