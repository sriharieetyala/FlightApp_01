export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  email: string;
  password: string;
  adminSecret?: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  email: string;
  role: string;
}

export interface SignupResponse {
  id: number;
  username: string;
  role: string;
}

