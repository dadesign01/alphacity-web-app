import { Sparkles, MapPin, Calendar, Users, Gift, CheckCircle, AlertCircle } from "lucide-react";
import { useState } from "react";

interface CampaignInput {
  eventName: string;
  location: string;
  startDate: string;
  endDate: string;
  expectedVisitors: string;
  campaignType: string;
  targetAudience: string;
  rewardCondition: string;
  spots: string;
}

export function CampaignDesign() {
  const [step, setStep] = useState<"input" | "result">("input");
  const [formData, setFormData] = useState<CampaignInput>({
    eventName: "",
    location: "",
    startDate: "",
    endDate: "",
    expectedVisitors: "",
    campaignType: "",
    targetAudience: "",
    rewardCondition: "",
    spots: "",
  });

  const [generatedResult, setGeneratedResult] = useState<any>(null);

  const handleInputChange = (field: keyof CampaignInput, value: string) => {
    setFormData({ ...formData, [field]: value });
  };

  const handleGenerate = () => {
    // AI 자동 생성 시뮬레이션
    const result = {
      summary: {
        concept: `${formData.eventName}은(는) ${formData.targetAudience}을(를) 대상으로 ${formData.location}에서 진행되는 ${formData.campaignType} 캠페인입니다. 참여자는 지정된 장소를 방문하여 미션을 완료하고 리워드를 획득할 수 있습니다.`,
        intro: `${formData.eventName}에 오신 것을 환영합니다! ${formData.location}의 주요 명소를 탐방하며 특별한 추억을 만들어보세요.`,
      },
      missions: [
        {
          name: "웰컴 미션",
          description: "행사장 입구에서 체크인하고 첫 스탬프를 받아보세요",
          type: "체크인",
        },
        {
          name: "탐방 미션",
          description: "지정된 3곳 이상의 장소를 방문하여 스탬프를 수집하세요",
          type: "스탬프",
        },
        {
          name: "완주 미션",
          description: "모든 스팟을 방문하고 최종 리워드를 받아가세요",
          type: "완료",
        },
      ],
      participantGuide: `본 캠페인은 ${formData.startDate}부터 ${formData.endDate}까지 진행됩니다. 참여자는 앱을 통해 각 장소에서 QR 코드를 스캔하거나 위치 인증을 통해 스탬프를 획득할 수 있습니다. ${formData.rewardCondition}을(를) 충족하면 리워드를 받을 수 있습니다.`,
      risks: [
        {
          category: "혼잡 가능성",
          level: "보통",
          guide: "예상 방문자 분산을 위해 시간대별 인센티브 제공 검토",
        },
        {
          category: "GPS 오인증",
          level: "낮음",
          guide: "QR 코드 병행 인증 방식으로 정확도 향상",
        },
        {
          category: "리워드 소진",
          level: "보통",
          guide: "일일 리워드 수량 제한 및 재고 모니터링 필요",
        },
        {
          category: "민원 발생",
          level: "낮음",
          guide: "현장 안내 스태프 배치 및 FAQ 사전 준비",
        },
      ],
      checklist: {
        before: [
          "스팟 위치 GPS 좌표 최종 확인",
          "QR 코드 출력 및 현장 부착",
          "리워드 수량 확보 및 인계",
          "운영 스태프 교육 완료",
          "앱 테스트 및 오류 점검",
        ],
        during: [
          "참여자 현황 실시간 모니터링",
          "리워드 재고 수시 확인",
          "민원 대응 및 기록",
          "혼잡 구역 현장 관리",
        ],
        after: [
          "참여 통계 데이터 수집",
          "참여자 피드백 취합",
          "미사용 리워드 회수",
          "결과 보고서 작성",
        ],
      },
      courses: [
        {
          name: "추천 코스 A",
          description: "주요 명소 중심 코스 (소요시간: 약 2시간)",
          spots: formData.spots.split(",").slice(0, 3).map((s) => s.trim()),
        },
        {
          name: "추천 코스 B",
          description: "전체 탐방 코스 (소요시간: 약 3시간)",
          spots: formData.spots.split(",").map((s) => s.trim()),
        },
      ],
    };

    setGeneratedResult(result);
    setStep("result");
  };

  const handleReset = () => {
    setStep("input");
    setGeneratedResult(null);
  };

  if (step === "result" && generatedResult) {
    return (
      <div className="p-8">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h2 className="text-2xl font-semibold text-gray-900">AI 캠페인 설계 결과</h2>
            <p className="text-sm text-gray-500 mt-1">자동 생성된 캠페인 구조를 확인하세요</p>
          </div>
          <button
            onClick={handleReset}
            className="px-4 py-2 bg-gray-600 text-white rounded-lg hover:bg-gray-700 transition-colors"
          >
            새로 만들기
          </button>
        </div>

        <div className="space-y-6">
          {/* 1. 캠페인 요약 */}
          <div className="bg-white rounded-lg border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">캠페인 요약</h3>
            <div className="space-y-3">
              <div>
                <p className="text-sm font-medium text-gray-500 mb-1">전체 콘셉트</p>
                <p className="text-sm text-gray-900">{generatedResult.summary.concept}</p>
              </div>
              <div>
                <p className="text-sm font-medium text-gray-500 mb-1">참여자 안내 문구</p>
                <p className="text-sm text-gray-900">{generatedResult.summary.intro}</p>
              </div>
            </div>
          </div>

          {/* 2. 추천 미션 구성 */}
          <div className="bg-white rounded-lg border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">추천 미션 구성</h3>
            <div className="space-y-3">
              {generatedResult.missions.map((mission: any, index: number) => (
                <div key={index} className="flex items-start gap-3 p-4 bg-gray-50 rounded-lg">
                  <div className="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 text-sm font-medium flex-shrink-0">
                    {index + 1}
                  </div>
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <p className="text-sm font-medium text-gray-900">{mission.name}</p>
                      <span className="text-xs px-2 py-0.5 bg-blue-100 text-blue-800 rounded">
                        {mission.type}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600">{mission.description}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 3. 참여자 안내 문구 */}
          <div className="bg-white rounded-lg border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">참여자 안내 문구</h3>
            <p className="text-sm text-gray-700 leading-relaxed">{generatedResult.participantGuide}</p>
          </div>

          {/* 4. 운영 리스크 진단 */}
          <div className="bg-white rounded-lg border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">운영 리스크 진단</h3>
            <div className="space-y-3">
              {generatedResult.risks.map((risk: any, index: number) => (
                <div key={index} className="flex items-start gap-3 p-4 border border-gray-200 rounded-lg">
                  <AlertCircle
                    className={`w-5 h-5 flex-shrink-0 ${
                      risk.level === "높음"
                        ? "text-red-500"
                        : risk.level === "보통"
                        ? "text-yellow-500"
                        : "text-green-500"
                    }`}
                  />
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <p className="text-sm font-medium text-gray-900">{risk.category}</p>
                      <span
                        className={`text-xs px-2 py-0.5 rounded ${
                          risk.level === "높음"
                            ? "bg-red-100 text-red-800"
                            : risk.level === "보통"
                            ? "bg-yellow-100 text-yellow-800"
                            : "bg-green-100 text-green-800"
                        }`}
                      >
                        {risk.level}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600">{risk.guide}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 5. 운영 체크리스트 */}
          <div className="bg-white rounded-lg border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">운영 체크리스트</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div>
                <p className="text-sm font-medium text-gray-700 mb-3">행사 전</p>
                <div className="space-y-2">
                  {generatedResult.checklist.before.map((item: string, index: number) => (
                    <div key={index} className="flex items-start gap-2">
                      <CheckCircle className="w-4 h-4 text-gray-400 flex-shrink-0 mt-0.5" />
                      <p className="text-sm text-gray-600">{item}</p>
                    </div>
                  ))}
                </div>
              </div>
              <div>
                <p className="text-sm font-medium text-gray-700 mb-3">행사 중</p>
                <div className="space-y-2">
                  {generatedResult.checklist.during.map((item: string, index: number) => (
                    <div key={index} className="flex items-start gap-2">
                      <CheckCircle className="w-4 h-4 text-gray-400 flex-shrink-0 mt-0.5" />
                      <p className="text-sm text-gray-600">{item}</p>
                    </div>
                  ))}
                </div>
              </div>
              <div>
                <p className="text-sm font-medium text-gray-700 mb-3">행사 종료 후</p>
                <div className="space-y-2">
                  {generatedResult.checklist.after.map((item: string, index: number) => (
                    <div key={index} className="flex items-start gap-2">
                      <CheckCircle className="w-4 h-4 text-gray-400 flex-shrink-0 mt-0.5" />
                      <p className="text-sm text-gray-600">{item}</p>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* 6. 추천 코스·동선 */}
          <div className="bg-white rounded-lg border border-gray-200 p-6">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">추천 코스·동선</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {generatedResult.courses.map((course: any, index: number) => (
                <div key={index} className="border border-gray-200 rounded-lg p-4">
                  <p className="text-sm font-medium text-gray-900 mb-2">{course.name}</p>
                  <p className="text-sm text-gray-600 mb-3">{course.description}</p>
                  <div className="space-y-2">
                    {course.spots.map((spot: string, spotIndex: number) => (
                      <div key={spotIndex} className="flex items-center gap-2">
                        <MapPin className="w-4 h-4 text-blue-600" />
                        <p className="text-sm text-gray-700">{spot}</p>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-gray-900">캠페인 자동 설계</h2>
          <p className="text-sm text-gray-500 mt-1">AI 기반으로 캠페인 구조를 자동 생성합니다</p>
        </div>
      </div>

      <div className="bg-white rounded-lg border border-gray-200 p-6">
        <div className="flex items-center gap-2 mb-6">
          <Sparkles className="w-5 h-5 text-blue-600" />
          <h3 className="text-lg font-semibold text-gray-900">기본 정보 입력</h3>
        </div>

        <div className="space-y-6">
          {/* 행사명 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              행사명 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formData.eventName}
              onChange={(e) => handleInputChange("eventName", e.target.value)}
              placeholder="예: 2025 봄꽃 스탬프 투어"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* 지역/장소 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              지역 / 장소 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formData.location}
              onChange={(e) => handleInputChange("location", e.target.value)}
              placeholder="예: 서울 여의도 한강공원"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* 기간 */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                시작일 <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                value={formData.startDate}
                onChange={(e) => handleInputChange("startDate", e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                종료일 <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                value={formData.endDate}
                onChange={(e) => handleInputChange("endDate", e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>

          {/* 예상 방문자 수 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              예상 방문자 수 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formData.expectedVisitors}
              onChange={(e) => handleInputChange("expectedVisitors", e.target.value)}
              placeholder="예: 5,000명"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* 캠페인 유형 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              캠페인 유형 <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.campaignType}
              onChange={(e) => handleInputChange("campaignType", e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">선택하세요</option>
              <option value="스탬프투어">스탬프투어</option>
              <option value="미션">미션</option>
              <option value="현장퀴즈">현장퀴즈</option>
              <option value="문화해설">문화해설</option>
            </select>
          </div>

          {/* 참여 대상 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              참여 대상 <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.targetAudience}
              onChange={(e) => handleInputChange("targetAudience", e.target.value)}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">선택하세요</option>
              <option value="가족">가족</option>
              <option value="MZ세대">MZ세대</option>
              <option value="관광객">관광객</option>
              <option value="지역주민">지역주민</option>
              <option value="전체">전체</option>
            </select>
          </div>

          {/* 리워드 조건 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              리워드 조건 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formData.rewardCondition}
              onChange={(e) => handleInputChange("rewardCondition", e.target.value)}
              placeholder="예: 5개 스팟 방문 시 기념품 증정"
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* 등록된 스팟 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              등록된 스팟 (POI) <span className="text-red-500">*</span>
            </label>
            <textarea
              value={formData.spots}
              onChange={(e) => handleInputChange("spots", e.target.value)}
              placeholder="예: 여의도 벚꽃길, 한강 공원, 63빌딩, 국회의사당 (쉼표로 구분)"
              rows={3}
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <p className="text-xs text-gray-500 mt-1">여러 장소는 쉼표(,)로 구분하여 입력하세요</p>
          </div>

          {/* 생성 버튼 */}
          <div className="flex justify-end pt-4">
            <button
              onClick={handleGenerate}
              disabled={
                !formData.eventName ||
                !formData.location ||
                !formData.startDate ||
                !formData.endDate ||
                !formData.expectedVisitors ||
                !formData.campaignType ||
                !formData.targetAudience ||
                !formData.rewardCondition ||
                !formData.spots
              }
              className="flex items-center gap-2 px-6 py-2.5 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
            >
              <Sparkles className="w-4 h-4" />
              AI 자동 생성
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
