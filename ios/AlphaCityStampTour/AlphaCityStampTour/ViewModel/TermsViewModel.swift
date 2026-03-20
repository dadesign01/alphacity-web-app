//
//  TermsViewModel.swift
//  AlphaCityStampTour
//

import Foundation

@MainActor
final class TermsViewModel: ObservableObject {
    @Published var title: String = ""
    @Published var content: String = ""
    @Published var isLoading = false

    private let repository = HomeRepository.shared

    func fetchTerms(type: String) async {
        isLoading = true
        do {
            let terms = try await repository.fetchTerms(type: type)
            if let activeTerm = terms.first {
                title = activeTerm.title
                content = activeTerm.content
            } else {
                title = ""
                content = ""
            }
        } catch {
            title = ""
            content = ""
        }
        isLoading = false
    }
}
