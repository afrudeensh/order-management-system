import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/auth.guard';
import { LoginComponent } from './features/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { NotificationsComponent } from './features/notifications/notifications.component';
import { ProductListComponent } from './features/products/product-list.component';
import { ProductFormComponent } from './features/products/product-form.component';
import { OrdersComponent } from './features/orders/orders.component';
export const routes: Routes = [
 { path: 'login', component: LoginComponent },
 { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard] },
 { path: 'products', component: ProductListComponent, canActivate: [authGuard] },
 { path: 'products/new', component: ProductFormComponent, canActivate: [authGuard, adminGuard] },
 { path: 'products/:id/edit', component: ProductFormComponent, canActivate: [authGuard, adminGuard] },
 { path: 'orders', component: OrdersComponent, canActivate: [authGuard] },
 { path: 'notifications', component: NotificationsComponent, canActivate: [authGuard] },
 { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
 { path: '**', redirectTo: 'dashboard' },
];
