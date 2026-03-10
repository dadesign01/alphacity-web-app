import { useState } from "react";
import {
  Calendar,
  Ticket,
  Stamp,
  CalendarCheck,
  Search,
  Plus,
  Save,
} from "lucide-react";

type TabType = "programs" | "coupons" | "stamps" | "events";

interface Store {
  id: string;
  name: string;
  category: string;
  owner: string;
  status: "Active" | "Pending" | "Disabled";
  contact: string;
  address: string;
}

// 샘플 데이터
const sampleStores: Store[] = [
  {
    id: "1",
    name: "카페 블루밍",
    category: "카페/디저트",
    owner: "김철수",
    status: "Active",
    contact: "02-1234-5678",
    address: "서울시 영등포구 여의도동 123",
  },
  {
    id: "2",
    name: "한강 레스토랑",
    category: "음식점",
    owner: "이영희",
    status: "Active",
    contact: "02-2345-6789",
    address: "서울시 영등포구 여의도동 456",
  },
];

const availablePrograms = [
  {
    id: "prog1",
    name: "2025 봄꽃 스탬프 투어",
    period: "2025-03-10 ~ 2025-04-30",
    status: "Active",
  },
  {
    id: "prog2",
    name: "여름 야시장 이벤트",
    period: "2025-06-01 ~ 2025-08-31",
    status: "Draft",
  },
];

const availableCoupons = [
  {
    id: "c1",
    name: "커피 할인 쿠폰",
    discount: "1,000원",
    expiry: "2025-12-31",
    status: "Active",
  },
  {
    id: "c2",
    name: "음식점 10% 할인",
    discount: "10%",
    expiry: "2025-12-31",
    status: "Active",
  },
];

const storeEvents = [
  {
    id: "e1",
    name: "카페 방문 이벤트",
    period: "2025-03-01 ~ 2025-03-31",
    participants: 245,
  },
  {
    id: "e2",
    name: "스탬프 2배 적립",
    period: "2025-04-01 ~ 2025-04-15",
    participants: 128,
  },
];

export function StoreMapping() {
  const [selectedStore, setSelectedStore] = useState<Store>(sampleStores[0]);
  const [activeTab, setActiveTab] = useState<TabType>("programs");
  const [searchTerm, setSearchTerm] = useState("");

  // 연결된 프로그램과 쿠폰
  const [selectedPrograms, setSelectedPrograms] = useState<string[]>(["prog1"]);
  const [acceptedCoupons, setAcceptedCoupons] = useState<{ [key: string]: boolean }>({
    c1: true,
    c2: false,
  });

  // 스탬프 발급 설정
  const [stampIssuance, setStampIssuance] = useState({
    stampType: "visit",
    issuanceTrigger: "qr_scan",
    dailyLimit: "5",
    duplicateAllowed: false,
  });

  const tabs = [
    { id: "programs" as TabType, label: "참여 프로그램", icon: Calendar },
    { id: "coupons" as TabType, label: "사용 가능 쿠폰", icon: Ticket },
    { id: "stamps" as TabType, label: "발급 가능 스탬프", icon: Stamp },
    { id: "events" as TabType, label: "상점 이벤트", icon: CalendarCheck },
  ];

  const toggleProgramSelection = (programId: string) => {
    if (selectedPrograms.includes(programId)) {
      setSelectedPrograms(selectedPrograms.filter((id) => id !== programId));
    } else {
      setSelectedPrograms([...selectedPrograms, programId]);
    }
  };

  const toggleCouponAcceptance = (couponId: string) => {
    setAcceptedCoupons({
      ...acceptedCoupons,
      [couponId]: !acceptedCoupons[couponId],
    });
  };

  const renderProgramsTab = () => {
    return (
      <div className="space-y-4">
        <div className="mb-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              placeholder="프로그램 검색..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        <div className="border border-gray-200 rounded-lg overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  선택
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  프로그램명
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  운영 기간
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  상태
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {availablePrograms.map((program) => (
                <tr key={program.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3">
                    <input
                      type="checkbox"
                      checked={selectedPrograms.includes(program.id)}
                      onChange={() => toggleProgramSelection(program.id)}
                      className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                    />
                  </td>
                  <td className="px-4 py-3 text-sm font-medium text-gray-900">
                    {program.name}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {program.period}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={`inline-block text-xs px-2 py-1 rounded ${
                        program.status === "Active"
                          ? "bg-green-100 text-green-800"
                          : "bg-yellow-100 text-yellow-800"
                      }`}
                    >
                      {program.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  const renderCouponsTab = () => {
    return (
      <div className="space-y-4">
        <div className="mb-4">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              placeholder="쿠폰 검색..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>

        <div className="space-y-3">
          {availableCoupons.map((coupon) => (
            <div
              key={coupon.id}
              className="flex items-center justify-between p-4 border border-gray-200 rounded-lg hover:bg-gray-50"
            >
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-2">
                  <p className="text-sm font-medium text-gray-900">{coupon.name}</p>
                  <span className="text-xs px-2 py-0.5 bg-blue-100 text-blue-800 rounded">
                    {coupon.discount}
                  </span>
                </div>
                <div className="flex items-center gap-4 text-xs text-gray-500">
                  <span>유효기간: {coupon.expiry}</span>
                  <span
                    className={`px-2 py-0.5 rounded ${
                      coupon.status === "Active"
                        ? "bg-green-100 text-green-800"
                        : "bg-gray-100 text-gray-800"
                    }`}
                  >
                    {coupon.status}
                  </span>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <span className="text-sm text-gray-600">수락</span>
                <label className="relative inline-flex items-center cursor-pointer">
                  <input
                    type="checkbox"
                    checked={acceptedCoupons[coupon.id] || false}
                    onChange={() => toggleCouponAcceptance(coupon.id)}
                    className="sr-only peer"
                  />
                  <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
                </label>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderStampsTab = () => {
    return (
      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <h4 className="text-sm font-medium text-gray-900 mb-4">
          스탬프 발급 설정
        </h4>

        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              스탬프 유형
            </label>
            <select
              value={stampIssuance.stampType}
              onChange={(e) =>
                setStampIssuance({ ...stampIssuance, stampType: e.target.value })
              }
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="visit">방문 스탬프</option>
              <option value="purchase">구매 스탬프</option>
              <option value="event">이벤트 스탬프</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              발급 트리거
            </label>
            <select
              value={stampIssuance.issuanceTrigger}
              onChange={(e) =>
                setStampIssuance({
                  ...stampIssuance,
                  issuanceTrigger: e.target.value,
                })
              }
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="qr_scan">QR 스캔</option>
              <option value="visit_verify">방문 인증</option>
              <option value="purchase_verify">구매 인증</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              일일 발급 제한
            </label>
            <input
              type="number"
              value={stampIssuance.dailyLimit}
              onChange={(e) =>
                setStampIssuance({ ...stampIssuance, dailyLimit: e.target.value })
              }
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              min="1"
            />
            <p className="text-xs text-gray-500 mt-1">
              하루에 최대 발급 가능한 스탬프 수
            </p>
          </div>

          <div className="flex items-center justify-between p-4 bg-gray-50 rounded-lg">
            <div>
              <p className="text-sm font-medium text-gray-900">중복 발급 허용</p>
              <p className="text-xs text-gray-500">
                같은 사용자가 여러 번 스탬프를 받을 수 있습니다
              </p>
            </div>
            <label className="relative inline-flex items-center cursor-pointer">
              <input
                type="checkbox"
                checked={stampIssuance.duplicateAllowed}
                onChange={(e) =>
                  setStampIssuance({
                    ...stampIssuance,
                    duplicateAllowed: e.target.checked,
                  })
                }
                className="sr-only peer"
              />
              <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
            </label>
          </div>
        </div>
      </div>
    );
  };

  const renderEventsTab = () => {
    return (
      <div className="space-y-4">
        <div className="flex justify-between items-center mb-4">
          <p className="text-sm text-gray-600">
            총 {storeEvents.length}개의 이벤트가 연결되어 있습니다
          </p>
          <div className="flex gap-2">
            <button className="flex items-center gap-2 px-4 py-2 text-sm border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors">
              <Plus className="w-4 h-4" />
              기존 이벤트 연결
            </button>
            <button className="flex items-center gap-2 px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
              <Plus className="w-4 h-4" />
              새 이벤트 만들기
            </button>
          </div>
        </div>

        <div className="border border-gray-200 rounded-lg overflow-hidden">
          <table className="w-full">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  이벤트명
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  운영 기간
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  참여자 수
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                  관리
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {storeEvents.map((event) => (
                <tr key={event.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 text-sm font-medium text-gray-900">
                    {event.name}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {event.period}
                  </td>
                  <td className="px-4 py-3 text-sm text-gray-600">
                    {event.participants.toLocaleString()}명
                  </td>
                  <td className="px-4 py-3">
                    <button className="text-sm text-blue-600 hover:underline">
                      상세보기
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-900">상점 연결 설정</h2>
        <p className="text-sm text-gray-500 mt-1">
          상점에 프로그램, 쿠폰, 스탬프 발급 규칙 및 이벤트를 연결합니다
        </p>
      </div>

      {/* Store Selector */}
      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            상점 선택
          </label>
          <select
            value={selectedStore.id}
            onChange={(e) => {
              const store = sampleStores.find((s) => s.id === e.target.value);
              if (store) setSelectedStore(store);
            }}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {sampleStores.map((store) => (
              <option key={store.id} value={store.id}>
                {store.name}
              </option>
            ))}
          </select>
        </div>

        {/* Store Info Card */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            <div>
              <p className="text-xs text-gray-500 mb-1">상점명</p>
              <p className="text-sm font-medium text-gray-900">
                {selectedStore.name}
              </p>
            </div>
            <div>
              <p className="text-xs text-gray-500 mb-1">카테고리</p>
              <p className="text-sm font-medium text-gray-900">
                {selectedStore.category}
              </p>
            </div>
            <div>
              <p className="text-xs text-gray-500 mb-1">대표자</p>
              <p className="text-sm font-medium text-gray-900">
                {selectedStore.owner}
              </p>
            </div>
            <div>
              <p className="text-xs text-gray-500 mb-1">상태</p>
              <span
                className={`inline-block text-xs px-2 py-1 rounded ${
                  selectedStore.status === "Active"
                    ? "bg-green-100 text-green-800"
                    : selectedStore.status === "Pending"
                    ? "bg-yellow-100 text-yellow-800"
                    : "bg-gray-100 text-gray-800"
                }`}
              >
                {selectedStore.status}
              </span>
            </div>
            <div>
              <p className="text-xs text-gray-500 mb-1">연락처</p>
              <p className="text-sm font-medium text-gray-900">
                {selectedStore.contact}
              </p>
            </div>
            <div>
              <p className="text-xs text-gray-500 mb-1">주소</p>
              <p className="text-sm font-medium text-gray-900">
                {selectedStore.address}
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="bg-white rounded-lg border border-gray-200">
        <div className="border-b border-gray-200">
          <div className="flex overflow-x-auto">
            {tabs.map((tab) => {
              const Icon = tab.icon;
              return (
                <button
                  key={tab.id}
                  onClick={() => setActiveTab(tab.id)}
                  className={`flex items-center gap-2 px-6 py-3 text-sm font-medium border-b-2 transition-colors whitespace-nowrap ${
                    activeTab === tab.id
                      ? "border-blue-600 text-blue-600"
                      : "border-transparent text-gray-500 hover:text-gray-700"
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {tab.label}
                </button>
              );
            })}
          </div>
        </div>

        <div className="p-6">
          {activeTab === "programs" && renderProgramsTab()}
          {activeTab === "coupons" && renderCouponsTab()}
          {activeTab === "stamps" && renderStampsTab()}
          {activeTab === "events" && renderEventsTab()}
        </div>
      </div>

      {/* Save Button */}
      <div className="fixed bottom-8 right-8">
        <button className="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg shadow-lg hover:bg-blue-700 transition-colors">
          <Save className="w-4 h-4" />
          저장하기
        </button>
      </div>
    </div>
  );
}
