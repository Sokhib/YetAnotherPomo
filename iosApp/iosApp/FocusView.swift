import SwiftUI
import KMPObservableViewModelSwiftUI
import Shared

private let presets: [Int32] = [15, 25, 50]

/// The iOS Home screen, built from the same Organic tokens the Compose Home screen uses.
///
/// Two deliberate differences from `HomeScreen.kt`, both consequences of the platform rather than
/// of taste: there is no accessibility banner and no allowlist dock, because iOS can neither
/// enumerate installed apps nor block them. Everything else - the dial, the presets, the Begin
/// pill, the type scale and the palette - is the same design language driven by the same
/// `FocusViewModel`.
struct FocusView: View {
    @StateViewModel
    var viewModel = makeFocusViewModel()

    @State private var confirmingEnd = false

    var body: some View {
        // `viewModel.uiState` is a plain FocusUiState, NOT a StateFlow - that typed property is
        // what @NativeCoroutinesState generated.
        let state = viewModel.uiState

        ZStack {
            Organic.colors.bgColor.ignoresSafeArea()

            VStack(spacing: 0) {
                header

                Spacer(minLength: 0)

                if state.isRunning {
                    countdown(state: state)
                } else {
                    dial(state: state)
                }

                Spacer(minLength: 0)

                PillButton(
                    text: state.isRunning ? "Hold to end" : "Begin \(state.minutes) minutes",
                    action: { if !state.isRunning { viewModel.beginSession() } },
                    fullWidth: true,
                    enabled: !state.isRunning
                )
                .opacity(state.isRunning ? 0 : 1)
                .overlay {
                    if state.isRunning {
                        HoldToConfirmButton(
                            text: "Hold to end",
                            holdDurationMs: state.holdToEndMs,
                            onHoldComplete: { confirmingEnd = true },
                            borderColor: Organic.colors.dividerColor,
                            fillColor: Organic.colors.accentRamp.color200,
                            contentColor: Organic.colors.neutral.color700
                        )
                    }
                }
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 22)

            if confirmingEnd {
                ConfirmEndSheet(
                    bodyText: "You still have \(format(seconds: Int(state.remainingSeconds))) left on this session.",
                    onKeepFocusing: { confirmingEnd = false },
                    onEndSession: {
                        confirmingEnd = false
                        viewModel.endSessionEarly()
                    }
                )
            }
        }
        .animation(.easeInOut(duration: Organic.motion.standard.seconds), value: confirmingEnd)
    }

    private var header: some View {
        HStack {
            Text("Focus Lock")
                .organicText(Organic.type.heading3)
                .foregroundStyle(Organic.colors.textColor)
            Spacer()
        }
    }

    private func dial(state: FocusUiState) -> some View {
        VStack(spacing: 30) {
            CircularDial(
                minutes: Int(state.minutes),
                maxMinutes: Int(state.dialMaxMinutes),
                onMinutesChange: { viewModel.setMinutes(minutes: Int32($0)) }
            )

            HStack(spacing: 9) {
                ForEach(presets, id: \.self) { preset in
                    let selected = preset == state.minutes
                    PillButton(
                        text: "\(preset) min",
                        action: { viewModel.setMinutes(minutes: preset) },
                        variant: selected ? .primary : .secondary,
                        containerColor: selected ? Organic.colors.accentRamp.color200 : .clear,
                        contentColor: selected ? Organic.colors.accentRamp.color800 : Organic.colors.neutral.color700,
                        borderColor: selected ? Organic.colors.accentRamp.color300 : Organic.colors.dividerColor
                    )
                }
            }
        }
    }

    private func countdown(state: FocusUiState) -> some View {
        VStack(spacing: 8) {
            Text(format(seconds: Int(state.remainingSeconds)))
                .organicText(Organic.type.clockDigits)
                .foregroundStyle(Organic.colors.textColor)
                .monospacedDigit()
            Text("REMAINING")
                .organicText(Organic.type.labelSmall)
                .foregroundStyle(Organic.colors.neutral.color600)
        }
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
