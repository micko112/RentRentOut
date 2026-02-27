import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoriesSidebarComponent } from './categories-sidebar.component';

describe('CategoriesSidebarComponent', () => {
  let component: CategoriesSidebarComponent;
  let fixture: ComponentFixture<CategoriesSidebarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoriesSidebarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CategoriesSidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
