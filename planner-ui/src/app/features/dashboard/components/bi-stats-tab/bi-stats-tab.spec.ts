import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BiStatsTab } from './bi-stats-tab';

describe('BiStatsTab', () => {
  let component: BiStatsTab;
  let fixture: ComponentFixture<BiStatsTab>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BiStatsTab],
    }).compileComponents();

    fixture = TestBed.createComponent(BiStatsTab);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
