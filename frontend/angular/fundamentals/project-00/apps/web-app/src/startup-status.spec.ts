import {
  completeStartupScreen,
  failStartupScreen,
  startStartupTimer,
} from './startup-status';

describe('startup status', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    document.body.innerHTML = `
      <div id="startup-screen">
        <div id="startup-loading">
          <span id="startup-elapsed"></span>
        </div>
        <div id="startup-error" hidden></div>
        <button id="startup-retry"></button>
      </div>
    `;
  });

  afterEach(() => {
    vi.useRealTimers();
    document.body.innerHTML = '';
  });

  it('shows elapsed time while startup is pending and removes the screen on success', () => {
    startStartupTimer();
    expect(document.getElementById('startup-elapsed')?.textContent).toBe('0 seconds elapsed');

    vi.advanceTimersByTime(2000);
    expect(document.getElementById('startup-elapsed')?.textContent).toBe('2 seconds elapsed');

    completeStartupScreen();
    expect(document.getElementById('startup-screen')).toBeNull();
  });

  it('shows an error and invokes the retry action when requested', () => {
    const onRetry = vi.fn();
    failStartupScreen(onRetry);

    expect(document.getElementById('startup-loading')?.hidden).toBe(true);
    expect(document.getElementById('startup-error')?.hidden).toBe(false);

    document.getElementById('startup-retry')?.click();
    expect(onRetry).toHaveBeenCalledOnce();
  });
});
