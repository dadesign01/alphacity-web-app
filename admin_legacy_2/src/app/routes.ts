import { createBrowserRouter } from "react-router";
import { Layout } from "@/app/components/layout";
import { Login } from "@/app/pages/login";
import { Dashboard } from "@/app/pages/dashboard";
import { CampaignDesign } from "@/app/pages/campaign-design";
import { Events } from "@/app/pages/events";
import { EventsManage } from "@/app/pages/events-manage";
import { MapManage } from "@/app/pages/map-manage";
import { MissionManage } from "@/app/pages/mission-manage";
import { CouponManage } from "@/app/pages/coupon-manage";
import { StoreManage } from "@/app/pages/store-manage";
import { NotificationManage } from "@/app/pages/notification-manage";
import { Settings } from "@/app/pages/settings";
import { ProgramMapping } from "@/app/pages/program-mapping";
import { StoreMapping } from "@/app/pages/store-mapping";

export const router = createBrowserRouter([
  {
    path: "/login",
    Component: Login,
  },
  {
    path: "/",
    Component: Layout,
    children: [
      { index: true, Component: Dashboard },
      { path: "campaign-design", Component: CampaignDesign },
      { path: "events", Component: Events },
      { path: "events-manage", Component: EventsManage },
      { path: "map-manage", Component: MapManage },
      { path: "mission-manage", Component: MissionManage },
      { path: "coupon-manage", Component: CouponManage },
      { path: "store-manage", Component: StoreManage },
      { path: "notification-manage", Component: NotificationManage },
      { path: "settings", Component: Settings },
      { path: "program-mapping", Component: ProgramMapping },
      { path: "store-mapping", Component: StoreMapping },
    ],
  },
]);