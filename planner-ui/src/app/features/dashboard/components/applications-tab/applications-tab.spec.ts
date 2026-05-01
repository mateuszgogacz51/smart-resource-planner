import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ApplicationsTab } from './applications-tab';

describe('ApplicationsTab', () => {
  let component: ApplicationsTab;
  let fixture: ComponentFixture<ApplicationsTab>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ApplicationsTab],
    }).compileComponents();

    fixture = TestBed.createComponent(ApplicationsTab);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
