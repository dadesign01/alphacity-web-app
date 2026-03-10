import { useState } from "react";
import {
  MapPin,
  Target,
  Stamp,
  Search,
  Plus,
  X,
  GripVertical,
  Save,
  Camera,
  HelpCircle,
  Clock,
  Upload,
  Trash2,
  ChevronDown,
  Check,
} from "lucide-react";

type TabType = "places" | "missions" | "stamps";

type MissionType = "location_checkin" | "photo" | "quiz" | "stay_duration";

interface Program {
  id: string;
  name: string;
  period: string;
  status: "Active" | "Draft" | "Closed";
  category: string;
  description: string;
  type: "Festival" | "Tour" | "Seminar" | "Event";
}

interface Place {
  id: string;
  name: string;
  type: string;
  category: string;
  address: string;
}

interface Mission {
  id: string;
  type: MissionType;
  name: string;
  linkedPlace?: string;
  verificationMethod?: string;
  completionCondition?: string;
  rewardStamp?: string;
  // 미션 타입별 추가 필드
  radius?: string; // location_checkin
  question?: string; // quiz
  answer?: string; // quiz
  duration?: string; // stay_duration
}

interface StampData {
  id: string;
  name: string;
  description: string;
  color: string;
  icon?: string;
  issuanceCondition: string;
}

// 샘플 데이터
const samplePrograms: Program[] = [
  {
    id: "1",
    name: "2025 봄꽃 스탬프 투어",
    period: "2025-03-10 ~ 2025-04-30",
    status: "Active",
    category: "문화관광",
    description: "봄꽃을 감상하며 즐기는 스탬프 투어 프로그램",
    type: "Tour",
  },
  {
    id: "2",
    name: "여름 세미나 2025",
    period: "2025-06-01 ~ 2025-08-31",
    status: "Draft",
    category: "교육",
    description: "여름 교육 세미나 프로그램",
    type: "Seminar",
  },
];

const availablePlaces: Place[] = [
  {
    id: "p1",
    name: "여의도 벚꽃길",
    type: "입구",
    category: "관광명소",
    address: "서울시 영등포구",
  },
  {
    id: "p2",
    name: "한강공원 포토존",
    type: "포토존",
    category: "공원",
    address: "서울시 영등포구",
  },
  {
    id: "p3",
    name: "푸드코트",
    type: "음식점",
    category: "편의시설",
    address: "서울시 영등포구",
  },
  {
    id: "p4",
    name: "국회의사당",
    type: "관광명소",
    category: "역사유적",
    address: "서울시 영등포구",
  },
];

const missionTemplates = [
  { type: "location_checkin" as MissionType, name: "위치 인증", icon: MapPin },
  { type: "photo" as MissionType, name: "사진 인증", icon: Camera },
  { type: "quiz" as MissionType, name: "퀴즈", icon: HelpCircle },
  { type: "stay_duration" as MissionType, name: "체류 시간", icon: Clock },
];

export function ProgramMapping() {
  const [selectedProgram, setSelectedProgram] = useState<Program>(samplePrograms[0]);
  const [activeTab, setActiveTab] = useState<TabType>("places");
  const [searchTerm, setSearchTerm] = useState("");

  // 장소 연결
  const [connectedPlaces, setConnectedPlaces] = useState<string[]>(["p1", "p2"]);

  // 미션 연결
  const [connectedMissions, setConnectedMissions] = useState<Mission[]>([
    {
      id: "m1",
      type: "location_checkin",
      name: "입구 체크인",
      linkedPlace: "p1",
      verificationMethod: "GPS",
      radius: "50",
      rewardStamp: "basic",
    },
  ]);

  // 스탬프 설정
  const [stamps, setStamps] = useState<StampData[]>([
    {
      id: "stamp1",
      name: "기본 스탬프",
      description: "장소 방문 시 발급",
      color: "#3B82F6",
      issuanceCondition: "mission_complete",
    },
  ]);

  // 스탬프 관련 UI 상태
  const [showStampDropdown, setShowStampDropdown] = useState(false);
  const [showStampLibrary, setShowStampLibrary] = useState(false);
  const [librarySearchTerm, setLibrarySearchTerm] = useState("");
  const [selectedLibraryStamps, setSelectedLibraryStamps] = useState<string[]>([]);

  // 스탬프 라이브러리 샘플 데이터
  const stampLibrary: StampData[] = [
    {
      id: "lib1",
      name: "벚꽃 스탬프",
      description: "봄 시즌 전용 스탬프",
      color: "#FF6B9D",
      issuanceCondition: "place_visit",
    },
    {
      id: "lib2",
      name: "방문 인증 스탬프",
      description: "기본 방문 인증용",
      color: "#4CAF50",
      issuanceCondition: "mission_complete",
    },
    {
      id: "lib3",
      name: "특별 이벤트 스탬프",
      description: "이벤트 전용",
      color: "#FFA726",
      issuanceCondition: "event_participate",
    },
  ];

  const tabs = [
    { id: "places" as TabType, label: "장소 연결", icon: MapPin },
    { id: "missions" as TabType, label: "미션 연결", icon: Target },
    { id: "stamps" as TabType, label: "스탬프 설정", icon: Stamp },
  ];

  // 장소 관련 함수
  const handleAddPlace = (placeId: string) => {
    if (!connectedPlaces.includes(placeId)) {
      setConnectedPlaces([...connectedPlaces, placeId]);
    }
  };

  const handleRemovePlace = (placeId: string) => {
    setConnectedPlaces(connectedPlaces.filter((p) => p !== placeId));
  };

  // 미션 관련 함수
  const handleAddMission = (type: MissionType) => {
    const newMission: Mission = {
      id: `m${Date.now()}`,
      type,
      name: `새 ${missionTemplates.find((t) => t.type === type)?.name} 미션`,
    };

    setConnectedMissions([...connectedMissions, newMission]);
  };

  const handleUpdateMission = (id: string, updates: Partial<Mission>) => {
    setConnectedMissions(
      connectedMissions.map((m) => (m.id === id ? { ...m, ...updates } : m))
    );
  };

  const handleRemoveMission = (id: string) => {
    setConnectedMissions(connectedMissions.filter((m) => m.id !== id));
  };

  // 스탬프 관련 함수
  const handleAddStamp = () => {
    const newStamp: StampData = {
      id: `stamp${Date.now()}`,
      name: "새 스탬프",
      description: "",
      color: "#3B82F6",
      issuanceCondition: "mission_complete",
    };
    setStamps([...stamps, newStamp]);
  };

  const handleUpdateStamp = (id: string, updates: Partial<StampData>) => {
    setStamps(stamps.map((s) => (s.id === id ? { ...s, ...updates } : s)));
  };

  const handleRemoveStamp = (id: string) => {
    setStamps(stamps.filter((s) => s.id !== id));
  };

  // 스탬프 라이브러리 관련 함수
  const handleAddLibraryStamp = (stampId: string) => {
    if (!selectedLibraryStamps.includes(stampId)) {
      setSelectedLibraryStamps([...selectedLibraryStamps, stampId]);
    }
  };

  const handleRemoveLibraryStamp = (stampId: string) => {
    setSelectedLibraryStamps(selectedLibraryStamps.filter((s) => s !== stampId));
  };

  const handleAddSelectedLibraryStamps = () => {
    const newStamps = selectedLibraryStamps.map((id) =>
      stampLibrary.find((s) => s.id === id)
    );
    setStamps([...stamps, ...newStamps.filter((s) => s) as StampData[]]);
    setSelectedLibraryStamps([]);
    setShowStampLibrary(false);
  };

  // 장소 연결 탭 렌더링
  const renderPlacesTab = () => {
    return (
      <div className="grid grid-cols-2 gap-6">
        {/* Left Panel - Available Places */}
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <div className="mb-4">
            <h4 className="text-sm font-medium text-gray-900 mb-3">등록된 장소</h4>
            <div className="relative mb-3">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                type="text"
                placeholder="장소 검색..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <select className="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
              <option value="">전체 유형</option>
              <option value="entrance">입구</option>
              <option value="photo">포토존</option>
              <option value="food">음식점</option>
              <option value="landmark">관광명소</option>
            </select>
          </div>

          <div className="space-y-2 max-h-96 overflow-y-auto">
            {availablePlaces.map((place) => (
              <div
                key={place.id}
                className="flex items-center justify-between p-3 border border-gray-200 rounded-lg hover:bg-gray-50"
              >
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">
                    {place.name}
                  </p>
                  <div className="flex items-center gap-2 mt-1">
                    <span className="text-xs px-2 py-0.5 bg-blue-100 text-blue-800 rounded">
                      {place.type}
                    </span>
                    <p className="text-xs text-gray-500">{place.address}</p>
                  </div>
                </div>
                {!connectedPlaces.includes(place.id) && (
                  <button
                    onClick={() => handleAddPlace(place.id)}
                    className="ml-2 p-1 text-blue-600 hover:bg-blue-50 rounded"
                  >
                    <Plus className="w-4 h-4" />
                  </button>
                )}
              </div>
            ))}
          </div>
        </div>

        {/* Right Panel - Connected Places */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <h4 className="text-sm font-medium text-gray-900 mb-3">
            연결된 장소 ({connectedPlaces.length})
          </h4>

          <div className="space-y-2">
            {connectedPlaces.map((placeId, index) => {
              const place = availablePlaces.find((p) => p.id === placeId);
              if (!place) return null;

              return (
                <div
                  key={placeId}
                  className="flex items-center gap-2 p-3 bg-white border border-gray-200 rounded-lg"
                >
                  <GripVertical className="w-4 h-4 text-gray-400 cursor-move" />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">
                      {place.name}
                    </p>
                    <div className="flex items-center gap-2 mt-1">
                      <span className="text-xs px-2 py-0.5 bg-blue-100 text-blue-800 rounded">
                        {place.type}
                      </span>
                      <p className="text-xs text-gray-500">순서: {index + 1}</p>
                    </div>
                  </div>
                  <button
                    onClick={() => handleRemovePlace(placeId)}
                    className="p-1 text-red-600 hover:bg-red-50 rounded"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
              );
            })}

            {connectedPlaces.length === 0 && (
              <div className="text-center py-8 text-sm text-gray-500">
                연결된 장소가 없습니다
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  // 미션 연결 탭 렌더링
  const renderMissionsTab = () => {
    const isSeminar = selectedProgram.type === "Seminar";

    return (
      <div className="space-y-6">
        {/* Mission Templates */}
        <div className="bg-white border border-gray-200 rounded-lg p-4">
          <h4 className="text-sm font-medium text-gray-900 mb-3">
            미션 추가하기
          </h4>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            {missionTemplates.map((template) => {
              const Icon = template.icon;
              const isDisabled =
                template.type === "stay_duration" && !isSeminar;

              return (
                <button
                  key={template.type}
                  onClick={() => handleAddMission(template.type)}
                  disabled={isDisabled}
                  className={`flex flex-col items-center gap-2 p-4 border rounded-lg transition-colors ${
                    isDisabled
                      ? "border-gray-200 bg-gray-50 cursor-not-allowed opacity-50"
                      : "border-gray-300 hover:border-blue-500 hover:bg-blue-50"
                  }`}
                >
                  <Icon className="w-6 h-6 text-blue-600" />
                  <span className="text-sm font-medium text-gray-900">
                    {template.name}
                  </span>
                  {isDisabled && (
                    <span className="text-xs text-gray-500">
                      세미나 전용
                    </span>
                  )}
                </button>
              );
            })}
          </div>
        </div>

        {/* Connected Missions */}
        <div className="space-y-4">
          {connectedMissions.map((mission, index) => (
            <div
              key={mission.id}
              className="bg-white border border-gray-200 rounded-lg p-4"
            >
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 text-sm font-medium">
                    {index + 1}
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {mission.name}
                    </p>
                    <p className="text-xs text-gray-500">
                      {
                        missionTemplates.find((t) => t.type === mission.type)
                          ?.name
                      }
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => handleRemoveMission(mission.id)}
                  className="p-1 text-red-600 hover:bg-red-50 rounded"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>

              <div className="grid grid-cols-2 gap-4">
                {/* 미션 이름 */}
                <div className="col-span-2">
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    미션 이름
                  </label>
                  <input
                    type="text"
                    value={mission.name}
                    onChange={(e) =>
                      handleUpdateMission(mission.id, { name: e.target.value })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                {/* 연결 장소 */}
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    연결 장소
                  </label>
                  <select
                    value={mission.linkedPlace || ""}
                    onChange={(e) =>
                      handleUpdateMission(mission.id, {
                        linkedPlace: e.target.value,
                      })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">장소 선택</option>
                    {availablePlaces.map((place) => (
                      <option key={place.id} value={place.id}>
                        {place.name}
                      </option>
                    ))}
                  </select>
                </div>

                {/* 인증 방법 */}
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    인증 방법
                  </label>
                  <select
                    value={mission.verificationMethod || ""}
                    onChange={(e) =>
                      handleUpdateMission(mission.id, {
                        verificationMethod: e.target.value,
                      })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">선택하세요</option>
                    <option value="GPS">GPS</option>
                    <option value="QR">QR 코드</option>
                    <option value="Photo">사진 업로드</option>
                  </select>
                </div>

                {/* 위치 인증 - 반경 설정 */}
                {mission.type === "location_checkin" && (
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      인증 반경 (미터)
                    </label>
                    <input
                      type="number"
                      value={mission.radius || "50"}
                      onChange={(e) =>
                        handleUpdateMission(mission.id, {
                          radius: e.target.value,
                        })
                      }
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      min="10"
                    />
                  </div>
                )}

                {/* 퀴즈 - 질문과 답변 */}
                {mission.type === "quiz" && (
                  <>
                    <div className="col-span-2">
                      <label className="block text-xs font-medium text-gray-700 mb-1">
                        퀴즈 질문
                      </label>
                      <input
                        type="text"
                        value={mission.question || ""}
                        onChange={(e) =>
                          handleUpdateMission(mission.id, {
                            question: e.target.value,
                          })
                        }
                        placeholder="예: 이 건물의 이름은 무엇인가요?"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                    <div className="col-span-2">
                      <label className="block text-xs font-medium text-gray-700 mb-1">
                        정답
                      </label>
                      <input
                        type="text"
                        value={mission.answer || ""}
                        onChange={(e) =>
                          handleUpdateMission(mission.id, {
                            answer: e.target.value,
                          })
                        }
                        placeholder="예: 63빌딩"
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      />
                    </div>
                  </>
                )}

                {/* 체류 시간 - 시간 설정 */}
                {mission.type === "stay_duration" && (
                  <div>
                    <label className="block text-xs font-medium text-gray-700 mb-1">
                      체류 시간 (분)
                    </label>
                    <input
                      type="number"
                      value={mission.duration || "5"}
                      onChange={(e) =>
                        handleUpdateMission(mission.id, {
                          duration: e.target.value,
                        })
                      }
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                      min="1"
                    />
                  </div>
                )}

                {/* 보상 스탬프 */}
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    보상 스탬프
                  </label>
                  <select
                    value={mission.rewardStamp || ""}
                    onChange={(e) =>
                      handleUpdateMission(mission.id, {
                        rewardStamp: e.target.value,
                      })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">선택하세요</option>
                    {stamps.map((stamp) => (
                      <option key={stamp.id} value={stamp.id}>
                        {stamp.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
          ))}

          {connectedMissions.length === 0 && (
            <div className="text-center py-12 text-sm text-gray-500 bg-gray-50 border border-gray-200 rounded-lg">
              연결된 미션이 없습니다. 위에서 미션을 추가해주세요.
            </div>
          )}
        </div>
      </div>
    );
  };

  // 스탬프 설정 탭 렌더링
  const renderStampsTab = () => {
    return (
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <p className="text-sm text-gray-600">
            이 프로그램에 사용할 스탬프를 설정하세요
          </p>
          
          {/* Dropdown Button */}
          <div className="relative">
            <button
              onClick={() => setShowStampDropdown(!showStampDropdown)}
              className="flex items-center gap-2 px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
            >
              <Plus className="w-4 h-4" />
              스탬프 추가
              <ChevronDown className="w-4 h-4" />
            </button>

            {showStampDropdown && (
              <div className="absolute right-0 mt-2 w-56 bg-white border border-gray-200 rounded-lg shadow-lg z-10">
                <button
                  onClick={() => {
                    handleAddStamp();
                    setShowStampDropdown(false);
                  }}
                  className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-2 border-b border-gray-100"
                >
                  <Plus className="w-4 h-4 text-blue-600" />
                  새 스탬프 만들기
                </button>
                <button
                  onClick={() => {
                    setShowStampLibrary(true);
                    setShowStampDropdown(false);
                  }}
                  className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-50 flex items-center gap-2"
                >
                  <Upload className="w-4 h-4 text-blue-600" />
                  기존 스탬프 가져오기
                </button>
              </div>
            )}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {stamps.map((stamp) => (
            <div
              key={stamp.id}
              className="bg-white border border-gray-200 rounded-lg p-4"
            >
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div
                    className="w-12 h-12 rounded-lg flex items-center justify-center"
                    style={{ backgroundColor: stamp.color + "20" }}
                  >
                    <Stamp
                      className="w-6 h-6"
                      style={{ color: stamp.color }}
                    />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-gray-900">
                      {stamp.name}
                    </p>
                    <p className="text-xs text-gray-500">스탬프 미리보기</p>
                  </div>
                </div>
                <button
                  onClick={() => handleRemoveStamp(stamp.id)}
                  className="p-1 text-red-600 hover:bg-red-50 rounded"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>

              <div className="space-y-3">
                {/* 스탬프 이름 */}
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    스탬프 이름
                  </label>
                  <input
                    type="text"
                    value={stamp.name}
                    onChange={(e) =>
                      handleUpdateStamp(stamp.id, { name: e.target.value })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                {/* 스탬프 설명 */}
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    스탬프 설명
                  </label>
                  <textarea
                    value={stamp.description}
                    onChange={(e) =>
                      handleUpdateStamp(stamp.id, {
                        description: e.target.value,
                      })
                    }
                    rows={2}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="스탬프에 대한 설명을 입력하세요"
                  />
                </div>

                {/* 스탬프 색상 */}
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    스탬프 색상
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="color"
                      value={stamp.color}
                      onChange={(e) =>
                        handleUpdateStamp(stamp.id, { color: e.target.value })
                      }
                      className="w-12 h-10 rounded border border-gray-300 cursor-pointer"
                    />
                    <input
                      type="text"
                      value={stamp.color}
                      onChange={(e) =>
                        handleUpdateStamp(stamp.id, { color: e.target.value })
                      }
                      className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </div>

                {/* 아이콘 업로드 */}
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    스탬프 아이콘
                  </label>
                  <button className="w-full px-3 py-2 border border-gray-300 border-dashed rounded-lg text-sm text-gray-600 hover:bg-gray-50 transition-colors flex items-center justify-center gap-2">
                    <Upload className="w-4 h-4" />
                    이미지 업로드
                  </button>
                </div>

                {/* 발급 조건 */}
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    발급 조건
                  </label>
                  <select
                    value={stamp.issuanceCondition}
                    onChange={(e) =>
                      handleUpdateStamp(stamp.id, {
                        issuanceCondition: e.target.value,
                      })
                    }
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="mission_complete">미션 완료</option>
                    <option value="place_visit">장소 방문</option>
                    <option value="event_participate">이벤트 참여</option>
                  </select>
                </div>

                {/* 연결 미션 (가져온 스탬프 편집용) */}
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    연결 미션
                  </label>
                  <select className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="">선택하세요</option>
                    {connectedMissions.map((mission) => (
                      <option key={mission.id} value={mission.id}>
                        {mission.name}
                      </option>
                    ))}
                  </select>
                </div>

                {/* 연결 장소 (가져온 스탬프 편집용) */}
                <div>
                  <label className="block text-xs font-medium text-gray-700 mb-1">
                    연결 장소
                  </label>
                  <select className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                    <option value="">선택하세요</option>
                    {availablePlaces.map((place) => (
                      <option key={place.id} value={place.id}>
                        {place.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
            </div>
          ))}
        </div>

        {stamps.length === 0 && (
          <div className="text-center py-12 text-sm text-gray-500 bg-gray-50 border border-gray-200 rounded-lg">
            등록된 스탬프가 없습니다. 스탬프를 추가해주세요.
          </div>
        )}
      </div>
    );
  };

  return (
    <div className="p-8">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-gray-900">
          프로그램 연결 설정
        </h2>
        <p className="text-sm text-gray-500 mt-1">
          프로그램의 장소, 미션, 스탬프를 설정하고 구성합니다
        </p>
      </div>

      {/* Program Selector */}
      <div className="bg-white rounded-lg border border-gray-200 p-6 mb-6">
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            프로그램 선택
          </label>
          <select
            value={selectedProgram.id}
            onChange={(e) => {
              const program = samplePrograms.find((p) => p.id === e.target.value);
              if (program) setSelectedProgram(program);
            }}
            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            {samplePrograms.map((program) => (
              <option key={program.id} value={program.id}>
                {program.name}
              </option>
            ))}
          </select>
        </div>

        {/* Program Info Card */}
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div>
              <p className="text-xs text-gray-500 mb-1">프로그램명</p>
              <p className="text-sm font-medium text-gray-900">
                {selectedProgram.name}
              </p>
            </div>
            <div>
              <p className="text-xs text-gray-500 mb-1">운영 기간</p>
              <p className="text-sm font-medium text-gray-900">
                {selectedProgram.period}
              </p>
            </div>
            <div>
              <p className="text-xs text-gray-500 mb-1">상태</p>
              <span
                className={`inline-block text-xs px-2 py-1 rounded ${
                  selectedProgram.status === "Active"
                    ? "bg-green-100 text-green-800"
                    : selectedProgram.status === "Draft"
                    ? "bg-yellow-100 text-yellow-800"
                    : "bg-gray-100 text-gray-800"
                }`}
              >
                {selectedProgram.status}
              </span>
            </div>
            <div>
              <p className="text-xs text-gray-500 mb-1">카테고리</p>
              <p className="text-sm font-medium text-gray-900">
                {selectedProgram.category}
              </p>
            </div>
          </div>
          <div className="mt-3 pt-3 border-t border-blue-200">
            <p className="text-xs text-gray-500 mb-1">설명</p>
            <p className="text-sm text-gray-700">{selectedProgram.description}</p>
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
          {activeTab === "places" && renderPlacesTab()}
          {activeTab === "missions" && renderMissionsTab()}
          {activeTab === "stamps" && renderStampsTab()}
        </div>
      </div>

      {/* Save Button */}
      <div className="fixed bottom-8 right-8">
        <button className="flex items-center gap-2 px-6 py-3 bg-blue-600 text-white rounded-lg shadow-lg hover:bg-blue-700 transition-colors">
          <Save className="w-4 h-4" />
          저장하기
        </button>
      </div>

      {/* 스탬프 라이브러리 모달 */}
      {showStampLibrary && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[80vh] overflow-hidden flex flex-col">
            <div className="p-6 border-b border-gray-200">
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold text-gray-900">
                  스탬프 라이브러리
                </h3>
                <button
                  onClick={() => {
                    setShowStampLibrary(false);
                    setSelectedLibraryStamps([]);
                  }}
                  className="p-1 text-gray-400 hover:text-gray-600"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="flex gap-3">
                <div className="flex-1 relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                  <input
                    type="text"
                    placeholder="스탬프 검색..."
                    value={librarySearchTerm}
                    onChange={(e) => setLibrarySearchTerm(e.target.value)}
                    className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
                <select className="px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
                  <option value="">전체 조건</option>
                  <option value="mission_complete">미션 완료</option>
                  <option value="place_visit">장소 방문</option>
                  <option value="event_participate">이벤트 참여</option>
                </select>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto p-6">
              <div className="border border-gray-200 rounded-lg overflow-hidden">
                <table className="w-full">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase w-12">
                        선택
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase w-16">
                        아이콘
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                        스탬프명
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                        설명
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                        생성일
                      </th>
                    </tr>
                  </thead>
                  <tbody className="bg-white divide-y divide-gray-200">
                    {stampLibrary
                      .filter((stamp) =>
                        stamp.name
                          .toLowerCase()
                          .includes(librarySearchTerm.toLowerCase())
                      )
                      .map((stamp) => (
                        <tr
                          key={stamp.id}
                          className="hover:bg-gray-50 cursor-pointer"
                          onClick={() => {
                            if (selectedLibraryStamps.includes(stamp.id)) {
                              handleRemoveLibraryStamp(stamp.id);
                            } else {
                              handleAddLibraryStamp(stamp.id);
                            }
                          }}
                        >
                          <td className="px-4 py-3">
                            <input
                              type="checkbox"
                              checked={selectedLibraryStamps.includes(stamp.id)}
                              onChange={() => {}}
                              className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                            />
                          </td>
                          <td className="px-4 py-3">
                            <div
                              className="w-10 h-10 rounded-lg flex items-center justify-center"
                              style={{ backgroundColor: stamp.color + "20" }}
                            >
                              <Stamp
                                className="w-5 h-5"
                                style={{ color: stamp.color }}
                              />
                            </div>
                          </td>
                          <td className="px-4 py-3 text-sm font-medium text-gray-900">
                            {stamp.name}
                          </td>
                          <td className="px-4 py-3 text-sm text-gray-600">
                            {stamp.description}
                          </td>
                          <td className="px-4 py-3 text-sm text-gray-500">
                            2025-03-01
                          </td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              </div>
            </div>

            <div className="p-6 border-t border-gray-200 flex items-center justify-between">
              <p className="text-sm text-gray-600">
                {selectedLibraryStamps.length}개 선택됨
              </p>
              <div className="flex gap-2">
                <button
                  onClick={() => {
                    setShowStampLibrary(false);
                    setSelectedLibraryStamps([]);
                  }}
                  className="px-4 py-2 text-sm border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                >
                  취소
                </button>
                <button
                  onClick={handleAddSelectedLibraryStamps}
                  disabled={selectedLibraryStamps.length === 0}
                  className="px-4 py-2 text-sm bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
                >
                  선택한 스탬프 추가
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}