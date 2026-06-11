//
//  AppleSignInCoordinator.swift
//  AlphaCityStampTour
//

import AuthenticationServices
import UIKit

/// Sign in with Apple 플로우를 실행하고 identityToken을 돌려주는 코디네이터
final class AppleSignInCoordinator: NSObject {

    struct AppleSignInResult {
        let identityToken: String
        let nickname: String?
    }

    private var completion: ((Result<AppleSignInResult, Error>) -> Void)?

    func signIn(completion: @escaping (Result<AppleSignInResult, Error>) -> Void) {
        self.completion = completion

        let request = ASAuthorizationAppleIDProvider().createRequest()
        request.requestedScopes = [.fullName, .email]

        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        controller.performRequests()
    }
}

extension AppleSignInCoordinator: ASAuthorizationControllerDelegate {
    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let credential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let tokenData = credential.identityToken,
              let identityToken = String(data: tokenData, encoding: .utf8) else {
            completion?(.failure(ASAuthorizationError(.invalidResponse)))
            return
        }

        // fullName은 최초 로그인 시에만 내려옴
        var nickname: String?
        if let fullName = credential.fullName {
            let formatted = PersonNameComponentsFormatter().string(from: fullName)
                .trimmingCharacters(in: .whitespaces)
            if !formatted.isEmpty { nickname = formatted }
        }

        completion?(.success(AppleSignInResult(identityToken: identityToken, nickname: nickname)))
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        completion?(.failure(error))
    }
}

extension AppleSignInCoordinator: ASAuthorizationControllerPresentationContextProviding {
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        let keyWindow = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first { $0.isKeyWindow }
        return keyWindow ?? ASPresentationAnchor()
    }
}
