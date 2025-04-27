import { Navigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

export function PrivateRoute({ children }: { children: React.ReactElement }) {
    const { isAuthenticated } = useAuth();

    if (!isAuthenticated) {
        return <Navigate to="/login" replace />;
    }

    return children;
}
