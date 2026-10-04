export interface AuthResponse{
    token:string;
    userId:number;
    name:string;
    role:'USER'|'ADMIN';
}

export interface Product{
    id:number;
    name:string;
    price:number;
    stock:number;
}

export interface ProductRequest{
    name:string;
    price:number;
    stock:number;
}

export interface OrderItemRequest{
    productId:number;
    quantity:number;
}

export interface OrderItem{
    productId:number;
    productName:string;
    quantity:number;
    unitPrice:number;
    lineTotal:number;
}

export interface Order{
    id:number;
    userId:number;
    totalAmount:number;
    status:string;
    createdAt:string;
    items:OrderItem[];
}

export interface AppNotification{
    id:number;
    userId:number;
    orderId:number;
    message:string;
    createdAt:string;
}