import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourcesTab } from './resources-tab';

describe('ResourcesTab', () => {
  let component: ResourcesTab;
  let fixture: ComponentFixture<ResourcesTab>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ResourcesTab],
    }).compileComponents();

    fixture = TestBed.createComponent(ResourcesTab);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
