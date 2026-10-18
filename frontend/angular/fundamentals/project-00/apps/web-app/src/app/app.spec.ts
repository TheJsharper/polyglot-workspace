import { TestBed } from '@angular/core/testing';
import { provideShell } from '@project-00/shells-main';
import { App } from './app';

describe('App', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [App], providers: [provideShell()] });
  });

  it('renders the main shell with its header title', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('app-shell')).not.toBeNull();
    expect(el.querySelector('.title')?.textContent).toBe('University');
  });
});
