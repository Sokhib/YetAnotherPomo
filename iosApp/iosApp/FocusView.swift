import SwiftUI
import KMPObservableViewModelSwiftUI
import Shared

/// Deliberately unstyled. The point of this screen is to prove one thing end to end: the
/// countdown you are watching is produced by the same Kotlin `FocusViewModel` the Android app
/// uses - same repository, same DataStore, same 1s ticker - with no timer logic written in Swift.
struct FocusView: View {
    /// `@StateViewModel` is KMP-ObservableViewModel's counterpart to `@StateObject`: it owns the
    /// ViewModel and re-renders this view whenever an @NativeCoroutinesState property changes.
    @StateViewModel
    var viewModel = makeFocusViewModel()

    var body: some View {
        // `viewModel.uiState` is a plain FocusUiState, NOT a StateFlow. That typed property is
        // what @NativeCoroutinesState generated for us.
        let state = viewModel.uiState

        VStack(spacing: 32) {
            Text("Focus Lock")
                .font(.largeTitle.bold())

            Text(format(seconds: Int(state.remainingSeconds)))
                .font(.system(size: 64, weight: .semibold, design: .rounded))
                .monospacedDigit()

            Text(state.isRunning ? "Focusing" : "\(state.minutes) minutes")
                .foregroundStyle(.secondary)

            if !state.isRunning {
                Stepper(
                    "Duration: \(state.minutes) min",
                    onIncrement: { viewModel.setMinutes(minutes: state.minutes + 5) },
                    onDecrement: { viewModel.setMinutes(minutes: state.minutes - 5) }
                )
                .padding(.horizontal, 48)
            }

            Button(state.isRunning ? "End session" : "Begin") {
                if state.isRunning {
                    viewModel.endSessionEarly()
                } else {
                    viewModel.beginSession()
                }
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
        }
        .padding()
    }

    private func format(seconds: Int) -> String {
        String(format: "%02d:%02d", seconds / 60, seconds % 60)
    }
}

/// Kotlin top-level functions are exported as static members of a class named after their FILE:
/// `systemMonotonicClock()` lives in MonotonicClock.ios.kt, hence `MonotonicClock_iosKt`. Worth
/// knowing - it is the single most confusing part of reading a generated Kotlin framework header.
private func makeFocusViewModel() -> FocusViewModel {
    let deps = KoinDependencies()
    return FocusViewModel(
        repository: deps.focusRepository,
        installedAppsRepository: deps.installedAppsRepository,
        accessibilityStatusChecker: deps.accessibilityStatusChecker,
        clock: MonotonicClock_iosKt.systemMonotonicClock()
    )
}
