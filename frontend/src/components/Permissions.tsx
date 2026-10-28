"use client";
import {createContext,useContext} from "react";
import type {Row} from "@/lib/api";
const Permissions=createContext<ReadonlySet<string>>(new Set());
export const canAccess=(user:Row|null|undefined,permission:string)=>Array.isArray(user?.permissions)&&user.permissions.includes(permission);
export const canSee=(user:Row|null|undefined,menu:string)=>Array.isArray(user?.menus)&&user.menus.includes(menu);
export function PermissionProvider({user,children}:{user:Row;children:React.ReactNode}){return <Permissions.Provider value={new Set(user.permissions||[])}>{children}</Permissions.Provider>}
export function usePermissions(){const permissions=useContext(Permissions);return (permission:string)=>permissions.has(permission)}
export function Can({permission,children}:{permission:string;children:React.ReactNode}){return usePermissions()(permission)?<>{children}</>:null}
