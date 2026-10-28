const esbuild=require('esbuild'),path=require('node:path'),fs=require('node:fs');
const root=path.resolve(__dirname,'..');fs.mkdirSync(path.join(root,'test-results'),{recursive:true});
esbuild.buildSync({entryPoints:[path.join(__dirname,'entry.ts')],outfile:path.join(root,'test-results/components.cjs'),bundle:true,platform:'node',format:'cjs',jsx:'automatic',external:['react','react-dom','react/jsx-runtime'],alias:{'next/navigation':path.join(__dirname,'navigation.ts'),'@':path.join(root,'src')},loader:{'.css':'empty'}});
esbuild.buildSync({entryPoints:[path.join(root,'src/lib/api.ts')],outfile:path.join(root,'test-results/api.cjs'),bundle:true,platform:'node',format:'cjs'});
