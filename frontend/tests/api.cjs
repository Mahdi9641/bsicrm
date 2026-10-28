const assert=require('node:assert/strict'),fs=require('node:fs'),path=require('node:path');
const file=require.resolve('../test-results/api.cjs');global.window=new EventTarget();
const fresh=()=>{delete require.cache[file];return require(file)};
const response=(status,body,headers={})=>new Response(JSON.stringify(body),{status,headers:{'Content-Type':'application/json',...headers}});
(async()=>{
 const results=[];
 async function test(name,fn){await fn();results.push({name,result:'PASS'});console.log('PASS',name)}
 await test('تجمیع خواندن همزمان بدون کش دائمی',async()=>{let calls=0;global.fetch=async()=>{calls++;await new Promise(r=>setTimeout(r,15));return response(200,{ok:true})};const {api}=fresh();await Promise.all(Array.from({length:10},()=>api('/services')));assert.equal(calls,1);await api('/services');assert.equal(calls,2)});
 await test('عدم تکرار نوشتن پس از 429 و احترام به Retry-After',async()=>{let calls=0;global.fetch=async()=>{calls++;return response(429,{message:'Too many requests'},{'Retry-After':'60'})};const {api}=fresh();await assert.rejects(()=>api('/customers','POST',{}),e=>e.status===429&&e.retryAfterSeconds===60&&/[\u0600-\u06ff]/.test(e.message));await assert.rejects(()=>api('/customers','POST',{}),e=>e.status===429);assert.equal(calls,1)});
 await test('عدم خروج هنگام محدود شدن تمدید نشست',async()=>{let expired=0;const listener=()=>expired++;window.addEventListener('session-expired',listener);global.fetch=async p=>p.endsWith('/refresh')?response(429,{}, {'Retry-After':'5'}):response(401,{});await assert.rejects(()=>fresh().api('/customers'),e=>e.status===429);assert.equal(expired,0);window.removeEventListener('session-expired',listener)});
 await test('فارسی بودن قطع شبکه و خطای نامعتبر سرور',async()=>{global.fetch=async()=>{throw new TypeError('Failed to fetch')};await assert.rejects(()=>fresh().api('/services'),e=>e.status===0&&e.message.includes('ارتباط'));global.fetch=async()=>response(500,{message:'Internal server error',fields:[{message:'must not be blank'}]});await assert.rejects(()=>fresh().api('/services'),e=>!e.message.includes('must')&&!e.message.includes('Internal')&&/[\u0600-\u06ff]/.test(e.message))});
 await test('پاک شدن نشست نامعتبر پس از شکست تمدید',async()=>{let expired=0;const fn=()=>expired++;window.addEventListener('session-expired',fn);global.fetch=async()=>response(401,{});await assert.rejects(()=>fresh().api('/services'));assert.equal(expired,1);window.removeEventListener('session-expired',fn)});
 fs.writeFileSync(path.join(__dirname,'../test-results/api-results.json'),JSON.stringify(results,null,2));
})().catch(e=>{console.error(e);process.exitCode=1});
