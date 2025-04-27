import { useAtom } from "jotai";
import { api } from "../utils/axios";
import { authAtom } from "../atoms/authAtom";

export function useAuth() {
  const [user, setUser] = useAtom(authAtom);

  const fetchProfile = async () => {
    const res = await api.get("/auth/profile");
    setUser(res.data);
  };

  const logout = async () => {
    await api.post("/auth/logout");
    setUser(null);
  };

  return { user, isAuthenticated: !!user, setUser, fetchProfile, logout };
}
