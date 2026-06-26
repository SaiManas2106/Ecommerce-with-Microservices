import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import "./styles.css";

type Product = {
  id: number;
  sku: string;
  name: string;
  category: string;
  description?: string;
  price: number;
  stock: number;
  status: string;
};

type CartItem = {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
};

type Cart = {
  userId: string;
  items: CartItem[];
  totalItems: number;
  totalAmount: number;
};

const API_BASE = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";
const demoUserId = "demo-customer";

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: { "Content-Type": "application/json", ...(options.headers ?? {}) },
    ...options
  });
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || response.statusText);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json();
}

function App() {
  const [products, setProducts] = useState<Product[]>([]);
  const [cart, setCart] = useState<Cart | null>(null);
  const [email, setEmail] = useState("customer@example.com");
  const [password, setPassword] = useState("password123");
  const [token, setToken] = useState(localStorage.getItem("token") ?? "");
  const [message, setMessage] = useState("Ready");
  const [loading, setLoading] = useState(false);

  const cartCount = useMemo(() => cart?.totalItems ?? 0, [cart]);

  useEffect(() => {
    loadProducts();
    loadCart();
  }, []);

  async function loadProducts() {
    try {
      setProducts(await request<Product[]>("/product-service/api/products"));
    } catch {
      setProducts([]);
    }
  }

  async function loadCart() {
    try {
      setCart(await request<Cart>(`/cart-service/api/cart/${demoUserId}`));
    } catch {
      setCart(null);
    }
  }

  async function registerOrLogin(mode: "register" | "login") {
    setLoading(true);
    try {
      const body = mode === "register"
        ? { email, fullName: "Demo Customer", password }
        : { email, password };
      const result = await request<{ token: string }>(`/auth-service/api/auth/${mode}`, {
        method: "POST",
        body: JSON.stringify(body)
      });
      localStorage.setItem("token", result.token);
      setToken(result.token);
      setMessage(`${mode === "register" ? "Registered" : "Logged in"} successfully`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Auth failed");
    } finally {
      setLoading(false);
    }
  }

  async function seedProduct() {
    const suffix = Date.now().toString().slice(-5);
    const product = {
      sku: `SKU-${suffix}`,
      name: `Everyday Backpack ${suffix}`,
      category: "Bags",
      description: "Durable commuter backpack with laptop sleeve",
      price: 79.99,
      stock: 25,
      status: "ACTIVE"
    };
    setLoading(true);
    try {
      await request<Product>("/product-service/api/products", {
        method: "POST",
        body: JSON.stringify(product)
      });
      setMessage("Product seeded");
      await loadProducts();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Could not seed product");
    } finally {
      setLoading(false);
    }
  }

  async function addToCart(product: Product) {
    setLoading(true);
    try {
      const nextCart = await request<Cart>(`/cart-service/api/cart/${demoUserId}/items`, {
        method: "POST",
        body: JSON.stringify({ productId: product.id, quantity: 1 })
      });
      setCart(nextCart);
      setMessage(`${product.name} added to cart`);
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Could not add to cart");
    } finally {
      setLoading(false);
    }
  }

  async function checkout() {
    setLoading(true);
    try {
      const order = await request<{ id: number; status: string; totalAmount: number }>("/order-service/api/orders/checkout", {
        method: "POST",
        headers: token ? { Authorization: `Bearer ${token}` } : {},
        body: JSON.stringify({ userId: demoUserId })
      });
      setMessage(`Order #${order.id} created with status ${order.status}`);
      await loadCart();
      await loadProducts();
    } catch (error) {
      setMessage(error instanceof Error ? error.message : "Checkout failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main>
      <header className="topbar">
        <div>
          <h1>CommerceOps Storefront</h1>
          <p>Microservices demo storefront connected to Gateway, Auth, Product, Cart, Order, Kafka, and Payment.</p>
        </div>
        <div className="cart-pill">Cart: {cartCount}</div>
      </header>

      <section className="workspace">
        <aside>
          <h2>Customer</h2>
          <label>Email<input value={email} onChange={(event) => setEmail(event.target.value)} /></label>
          <label>Password<input type="password" value={password} onChange={(event) => setPassword(event.target.value)} /></label>
          <div className="button-row">
            <button onClick={() => registerOrLogin("register")} disabled={loading}>Register</button>
            <button onClick={() => registerOrLogin("login")} disabled={loading}>Login</button>
          </div>
          <h2>Cart</h2>
          {cart?.items.length ? (
            <div className="cart-list">
              {cart.items.map((item) => (
                <div className="cart-line" key={item.productId}>
                  <span>{item.productName}</span>
                  <strong>{item.quantity} x ${item.unitPrice.toFixed(2)}</strong>
                </div>
              ))}
              <div className="cart-total">Total ${cart.totalAmount.toFixed(2)}</div>
              <button className="checkout" onClick={checkout} disabled={loading}>Checkout</button>
            </div>
          ) : (
            <p className="muted">No active cart yet.</p>
          )}
        </aside>

        <section className="catalog">
          <div className="section-title">
            <h2>Catalog</h2>
            <button onClick={seedProduct} disabled={loading}>Seed Product</button>
          </div>
          <div className="grid">
            {products.map((product) => (
              <article key={product.id}>
                <div className="sku">{product.sku}</div>
                <h3>{product.name}</h3>
                <p>{product.description}</p>
                <div className="meta">
                  <span>{product.category}</span>
                  <span>{product.stock} in stock</span>
                </div>
                <div className="buy-row">
                  <strong>${product.price.toFixed(2)}</strong>
                  <button onClick={() => addToCart(product)} disabled={loading || product.stock < 1}>Add</button>
                </div>
              </article>
            ))}
          </div>
        </section>
      </section>

      <footer>{message}</footer>
    </main>
  );
}

createRoot(document.getElementById("root")!).render(<App />);
