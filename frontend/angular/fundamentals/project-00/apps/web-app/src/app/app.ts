import { ChangeDetectionStrategy, Component } from '@angular/core';
import { ShellComponent } from '@project-00/shells-main';

@Component({
  selector: 'app-root',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ShellComponent],
  template: '<app-shell appTitle="University" />',
})
export class App {}
