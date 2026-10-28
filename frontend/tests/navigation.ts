import {useSyncExternalStore} from 'react';
const listeners=new Set<()=>void>();let path='/';
const router={push:(value:string)=>{path=value;window.history.pushState({},'',value);listeners.forEach(fn=>fn())},replace:(value:string)=>{path=value;window.history.replaceState({},'',value);listeners.forEach(fn=>fn())}};
export const useRouter=()=>router;
export const usePathname=()=>useSyncExternalStore(fn=>{listeners.add(fn);return()=>{listeners.delete(fn)}},()=>path,()=>path);
export const useSearchParams=()=>new URLSearchParams(window.location.search);

export const testNavigate=(path:string)=>router.push(path);
