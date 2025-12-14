export interface LoginRequest {
  username: string;
  password: string;
}

export interface SignupRequest {
  username: string;
  password: string;
  adminSecret?: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
}

export interface SignupResponse {
  id: number;
  username: string;
  role: string;
}

