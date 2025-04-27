import { atom } from "jotai";
import { LoggedUserDTO } from "../types/auth.types";

export const authAtom = atom<LoggedUserDTO | null>(null);
