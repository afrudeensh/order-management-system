export interface AuthResponse {
  token: string;
  userId: number;
  name: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

export interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
  image?: string | null;   // small data URL, optional
  color?: string | null;   // "#RRGGBB", optional
}

export interface ProductRequest {
  name: string;
  price: number;
  stock: number;
  image?: string | null;
  color?: string | null;
}

export interface OrderItemRequest {
  productId: number;
  quantity: number;
}

export interface UserSummary {
  id: number;
  name: string;
  email: string;
}

export interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface PageQuery {
  page: number;
  size: number;
  sort: string;
  direction: string;
  search?: string;
}

export interface Order {
  id: number;
  userId: number;
  totalAmount: number;
  status: string;
  createdAt: string;
  items: OrderItem[];
}

export interface AppNotification {
  id: number;
  userId: number;
  orderId: number;
  message: string;
  createdAt: string;
}