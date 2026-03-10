import { Outlet, Link, useLocation } from "react-router";
import {
  LayoutDashboard,
  Calendar,
  CalendarCheck,
  Map,
  Target,
  Ticket,
  Store,
  Bell,
  Settings as SettingsIcon,
  Sparkles,
  Link2,
  ShoppingBag,
} from "lucide-react";

const menuItems = [
  { path: "/", label: "대시보드", icon: LayoutDashboard },
  { path: "/campaign-design", label: "캠페인 설계", icon: Sparkles },
  { path: "/events", label: "행사 관리", icon: Calendar },
  { path: "/events-manage", label: "이벤트 관리", icon: CalendarCheck },
  { path: "/map-manage", label: "지도 / 장소 관리", icon: Map },
  { path: "/mission-manage", label: "미션 관리", icon: Target },
  { path: "/coupon-manage", label: "쿠폰 관리", icon: Ticket },
  { path: "/store-manage", label: "상점 관리", icon: Store },
  { path: "/notification-manage", label: "알림 관리", icon: Bell },
  { path: "/program-mapping", label: "프로그램 연결 설정", icon: Link2 },
  { path: "/store-mapping", label: "상점 연결 설정", icon: ShoppingBag },
  { path: "/settings", label: "설정", icon: SettingsIcon },
];

export function Layout() {
  const location = useLocation();

  return (
    <div className="flex h-screen bg-gray-50">
      {/* 좌측 사이드바 */}
      <aside className="w-64 bg-white border-r border-gray-200 flex flex-col">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-xl font-semibold text-gray-900">스탬프 투어 관리자</h1>
          <p className="text-sm text-gray-500 mt-1">Festival Admin</p>
        </div>

        <nav className="flex-1 overflow-y-auto p-4">
          <ul className="space-y-1">
            {menuItems.map((item) => {
              const Icon = item.icon;
              const isActive =
                location.pathname === item.path ||
                (item.path !== "/" && location.pathname.startsWith(item.path));

              return (
                <li key={item.path}>
                  <Link
                    to={item.path}
                    className={`flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm transition-colors ${
                      isActive
                        ? "bg-blue-50 text-blue-600 font-medium"
                        : "text-gray-700 hover:bg-gray-100"
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
              <p className="text-sm font-medium text-gray-900 truncate">
                관리자
              </p>
              <p className="text-xs text-gray-500 truncate">admin@festival.kr</p>
            </div>
          </div>
        </div>
      </aside>

      {/* 우측 콘텐츠 영역 */}
      <main className="flex-1 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
}