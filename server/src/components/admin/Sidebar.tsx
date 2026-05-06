'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
  LayoutDashboard,
  Image as ImageIcon,
  Calendar,
  CalendarCheck,
  Map,
  Target,
  Stamp,
  Ticket,
  TicketCheck,
  Store,
  Users,
  Bell,
  Settings as SettingsIcon,
  Link2,
  FileText,
  PartyPopper,
} from 'lucide-react';

const menuItems = [
  { path: '/admin', label: '대시보드', icon: LayoutDashboard },
  { path: '/admin/festivals', label: '축제 관리', icon: PartyPopper },
  { path: '/admin/banners', label: '배너 관리', icon: ImageIcon },
  { path: '/admin/programs', label: '행사 관리', icon: Calendar },
  { path: '/admin/program-mapping', label: '프로그램 연결 설정', icon: Link2 },
  { path: '/admin/events', label: '이벤트 관리', icon: CalendarCheck },
  { path: '/admin/places', label: '지도 / 장소 관리', icon: Map },
  { path: '/admin/missions', label: '미션 관리', icon: Target },
  { path: '/admin/stamps', label: '스탬프 관리', icon: Stamp },
  { path: '/admin/coupons', label: '쿠폰 관리', icon: Ticket },
  { path: '/admin/coupons/usage', label: '쿠폰 사용 확인', icon: TicketCheck },
  { path: '/admin/stores', label: '상점 관리', icon: Store },
  { path: '/admin/users', label: '사용자 관리', icon: Users },
  { path: '/admin/notifications', label: '알림 관리', icon: Bell },
  { path: '/admin/settings', label: '설정', icon: SettingsIcon },
  { path: '/admin/terms', label: '약관 관리', icon: FileText },
];

export default function Sidebar() {
  const pathname = usePathname();

  return (
    <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">
      <div className="p-6 border-b border-gray-200">
        <h1 className="text-xl font-semibold text-gray-900">스탬프 투어 관리자</h1>
        <p className="text-sm text-gray-500 mt-1">Festival Admin</p>
      </div>

      <nav className="flex-1 overflow-y-auto p-4">
        <ul className="space-y-1">
          {menuItems.map((item) => {
            const Icon = item.icon;
            const isMatch = item.path === '/admin'
              ? pathname === '/admin'
              : pathname === item.path || pathname.startsWith(item.path + '/');
            const hasBetterMatch = isMatch && menuItems.some(
              (other) => other.path !== item.path && other.path.startsWith(item.path + '/') && (pathname === other.path || pathname.startsWith(other.path + '/'))
            );
            const isActive = isMatch && !hasBetterMatch;

            return (
              <li key={item.path}>
                <Link
                  href={item.path}
                  className={`flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm transition-colors ${
                    isActive
                      ? 'bg-blue-50 text-blue-600 font-medium'
                      : 'text-gray-700 hover:bg-gray-100'
                  }`}
                >
                  <Icon className="w-5 h-5" />
                  <span>{item.label}</span>
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      <div className="p-4 border-t border-gray-200">
        <div className="flex items-center gap-3 px-4 py-2">
          <div className="w-8 h-8 rounded-full bg-blue-500 flex items-center justify-center text-white text-sm">
            관
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium text-gray-900 truncate">관리자</p>
            <p className="text-xs text-gray-500 truncate">admin@festival.kr</p>
          </div>
        </div>
      </div>
    </aside>
  );
}
