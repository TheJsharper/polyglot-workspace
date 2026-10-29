let timer: number | undefined;

function requiredElement(id: string): HTMLElement {
  const element = document.getElementById(id);
  if (!element) {
    throw new Error(`Missing startup screen element: #${id}`);
  }
  return element;
}

function stopTimer(): void {
  if (timer !== undefined) {
    window.clearInterval(timer);
    timer = undefined;
  }
}

export function startStartupTimer(): void {
  stopTimer();
  const startedAt = Date.now();
  const elapsed = requiredElement('startup-elapsed');
  const updateElapsed = () => {
    const seconds = Math.floor((Date.now() - startedAt) / 1000);
    elapsed.textContent = `${seconds} ${seconds === 1 ? 'second' : 'seconds'} elapsed`;
  };

  updateElapsed();
  timer = window.setInterval(updateElapsed, 1000);
}

export function completeStartupScreen(): void {
  stopTimer();
  requiredElement('startup-screen').remove();
}

export function failStartupScreen(onRetry: () => void): void {
  stopTimer();
  requiredElement('startup-loading').hidden = true;
  requiredElement('startup-error').hidden = false;
  requiredElement('startup-retry').addEventListener('click', onRetry, { once: true });
}
