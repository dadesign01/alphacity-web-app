1️⃣ 프로그램 연결 설정 페이지 (Program Mapping)
글쓰기

Design an admin dashboard page titled "프로그램 연결 설정 (Program Mapping)".

Purpose:
Allow administrators to map and configure relationships between a selected program and existing system resources such as places, missions, stamps, coupons, events, and stores.

Layout:

Standard admin dashboard layout

Left sidebar navigation

Top header bar

Main content area with white cards

Use a clean SaaS admin UI style

Top Section (Program Selector Card):
Include a program selection area with:

Program dropdown selector

Program information card displaying:

Program name

Program period

Status (Active / Draft / Closed)

Category

Short description

Below the program card create a tab navigation interface for mapping resources.

Tabs:

장소 연결 (Places)

미션 연결 (Missions)

스탬프 규칙 (Stamp Rules)

쿠폰 연결 (Coupons)

이벤트 연결 (Events)

참여 상점 (Stores)

Each tab should include a dual panel mapping interface.

Left Panel:

List of registered items

Search input

Filter dropdown

Checkbox multi-select

Table style list

Right Panel:

Items already connected to the program

Remove button

Drag-and-drop sort order

Display connection status

Stamp Rules Tab:
Include configuration fields:

Condition type dropdown:

Visit place

Mission completion

Event participation

Store visit verification

Stamp amount input

Daily limit toggle

Duplicate allowance toggle

Coupon Tab:
Additional configuration fields:

Required stamp count

Distribution type:

Automatic

Manual

Target type:

All users

Condition users

Targeted users

Expiration date

Issuance limit per user

UI Components:

Tables

Checkboxes

Multi-select

Drag-and-drop ordering

Toggle switches

Save button fixed at bottom-right

Visual Style:

Modern SaaS admin dashboard

Card-based layout

Soft shadows

Blue primary color

Rounded corners

Minimalist design

2️⃣ 상점 연결 설정 페이지 (Store Mapping)
글쓰기

Design an admin dashboard page titled "상점 연결 설정 (Store Mapping)".

Purpose:
Allow administrators to connect stores to programs, coupons, stamp issuance rules, and store-specific events.

Layout:

Same admin dashboard layout

Left sidebar navigation

Top header bar

Main content using card UI

Top Section (Store Selector Card):

Store dropdown selector

Store information card displaying:

Store name

Category

Owner name

Status (Active / Pending / Disabled)

Contact information

Address

Tabs:

참여 프로그램 (Programs)

사용 가능 쿠폰 (Coupons)

발급 가능 스탬프 (Stamp Issuance)

상점 이벤트 (Store Events)

Programs Tab:

Table list of available programs

Checkbox selection to join program

Display program period and status

Coupons Tab:

Select coupons usable in this store

Toggle switch: "Accept coupon"

Show coupon expiration and status

Stamp Issuance Tab:
Configuration fields:

Stamp type dropdown

Issuance trigger:

QR scan

Visit verification

Purchase verification

Daily issuance limit

Duplicate allowance toggle

Store Events Tab:

List of events connected to the store

Button: "Connect Existing Event"

Button: "Create New Event"

UI Components:

Table lists

Toggle switches

Checkbox multi-select

Card containers

Save button at bottom-right

Visual Style:

Clean SaaS admin UI

White cards

Blue accent color

Soft shadows

Clear typography

3️⃣ 쿠폰 배포 정책 설정 페이지
글쓰기

Design an admin dashboard page titled "쿠폰 배포 정책 설정 (Coupon Distribution Rules)".

Purpose:
Allow administrators to define coupon distribution rules and targeting policies.

Layout:
Admin dashboard layout with sidebar and header.

Top Section:
Coupon selection card including:

Coupon dropdown selector

Coupon preview card showing:

Coupon name

Discount / reward description

Expiration period

Status

Distribution Policy Section:

Fields:
Distribution Scope:

All program participants

Users with required stamp count

Event winners

Manual assignment

Target Type:

All users

Condition users

Targeted users (1:1 mapping)

Condition Settings:

Required stamp count

Mission completion requirement

Event participation requirement

Targeted Users Section:

Upload user list (CSV upload)

Search user ID

Add individual users manually

Table preview of selected users

Usage Settings:

Expiration date

Usage limit per user

Duplicate usage toggle

Maximum total issuance

Preview Panel:
Display summary of coupon distribution rule.

UI Components:

Dropdowns

Toggle switches

Upload button

Table preview

Save button

4️⃣ 관리자 메뉴 구조

피그마에서 사이드바 메뉴도 같이 만들면 좋습니다.

운영 설정 (Operations)

├ 프로그램 연결 설정
├ 상점 연결 설정
└ 쿠폰 배포 정책
🔵 추가로 추천하는 피그마 프롬프트 (선택)

이거 하나 더 넣으면 전체 관리자 구조까지 자동 생성됩니다.

글쓰기

Design a modern SaaS admin dashboard for a "Stamp Tour Management System".

Include the following sidebar menu sections:

Dashboard

Program Management

Program List

Program Mapping

Resource Management

Missions

Stamps

Coupons

Places

Stores

Events

Operations

Store Mapping

Coupon Distribution Rules

Use a clean SaaS design style with:

Card layout

Data tables

Sidebar navigation

Blue primary color

Rounded UI components