import{a as Y}from"./chunk-3ISIQJW5.js";import{Fa as U,Ga as G,Ha as J,Ja as W,Ma as X,j as K,l as L,n as q,s as H}from"./chunk-HT6JNWVA.js";import{$b as A,Ab as f,Gb as h,Gc as Q,Hb as g,Ib as F,Jb as b,Kb as x,Mb as u,Rb as d,Sb as r,Ta as V,Tb as j,Ub as z,Vb as I,W as k,Wa as p,Xb as w,Yb as T,aa as $,ac as O,bc as R,ga as E,gb as M,ha as s,hc as B,ia as l,kb as P,la as y,mb as m,ra as C,vb as _,wb as c,wc as N,xb as S,zb as D}from"./chunk-SQFFUPMZ.js";var ee=["removeicon"],ie=["*"];function ne(e,a){if(e&1){let i=u();h(0,"img",4),d("error",function(t){s(i);let o=r();return l(o.imageError(t))}),g()}if(e&2){let i=r();c("src",i.image,V)("alt",i.alt)}}function te(e,a){if(e&1&&F(0,"span",6),e&2){let i=r(2);f(i.icon),c("ngClass","p-chip-icon"),_("data-pc-section","icon")}}function oe(e,a){if(e&1&&m(0,te,1,4,"span",5),e&2){let i=r();c("ngIf",i.icon)}}function re(e,a){if(e&1&&(h(0,"div",7),O(1),g()),e&2){let i=r();_("data-pc-section","label"),p(),R(i.label)}}function ce(e,a){if(e&1){let i=u();h(0,"span",11),d("click",function(t){s(i);let o=r(3);return l(o.close(t))})("keydown",function(t){s(i);let o=r(3);return l(o.onKeydown(t))}),g()}if(e&2){let i=r(3);f(i.removeIcon),c("ngClass","p-chip-remove-icon"),_("data-pc-section","removeicon")("aria-label",i.removeAriaLabel)}}function ae(e,a){if(e&1){let i=u();h(0,"TimesCircleIcon",12),d("click",function(t){s(i);let o=r(3);return l(o.close(t))})("keydown",function(t){s(i);let o=r(3);return l(o.onKeydown(t))}),g()}if(e&2){let i=r(3);f("p-chip-remove-icon"),_("data-pc-section","removeicon")("aria-label",i.removeAriaLabel)}}function pe(e,a){if(e&1&&(b(0),m(1,ce,1,5,"span",9)(2,ae,1,4,"TimesCircleIcon",10),x()),e&2){let i=r(2);p(),c("ngIf",i.removeIcon),p(),c("ngIf",!i.removeIcon)}}function se(e,a){}function le(e,a){e&1&&m(0,se,0,0,"ng-template")}function me(e,a){if(e&1){let i=u();h(0,"span",13),d("click",function(t){s(i);let o=r(2);return l(o.close(t))})("keydown",function(t){s(i);let o=r(2);return l(o.onKeydown(t))}),m(1,le,1,0,null,14),g()}if(e&2){let i=r(2);_("data-pc-section","removeicon")("aria-label",i.removeAriaLabel),p(),c("ngTemplateOutlet",i.removeIconTemplate||i._removeIconTemplate)}}function _e(e,a){if(e&1&&(b(0),m(1,pe,3,2,"ng-container",3)(2,me,2,3,"span",8),x()),e&2){let i=r();p(),c("ngIf",!i.removeIconTemplate&&!i._removeIconTemplate),p(),c("ngIf",i.removeIconTemplate||i._removeIconTemplate)}}var he=({dt:e})=>`
.p-chip {
    display: inline-flex;
    align-items: center;
    background: ${e("chip.background")};
    color: ${e("chip.color")};
    border-radius: ${e("chip.border.radius")};
    padding: ${e("chip.padding.y")} ${e("chip.padding.x")};
    gap: ${e("chip.gap")};
}

.p-chip-icon {
    color: ${e("chip.icon.color")};
    font-size: ${e("chip.icon.font.size")};
    width: ${e("chip.icon.size")};
    height: ${e("chip.icon.size")};
}

.p-chip-image {
    border-radius: 50%;
    width: ${e("chip.image.width")};
    height: ${e("chip.image.height")};
    margin-left: calc(-1 * ${e("chip.padding.y")});
}

.p-chip:has(.p-chip-remove-icon) {
    padding-inline-end: ${e("chip.padding.y")};
}

.p-chip:has(.p-chip-image) {
    padding-top: calc(${e("chip.padding.y")} / 2);
    padding-bottom: calc(${e("chip.padding.y")} / 2);
}

.p-chip-remove-icon {
    cursor: pointer;
    font-size: ${e("chip.remove.icon.font.size")};
    width: ${e("chip.remove.icon.size")};
    height: ${e("chip.remove.icon.size")};
    color: ${e("chip.remove.icon.color")};
    border-radius: 50%;
    transition: outline-color ${e("chip.transition.duration")}, box-shadow ${e("chip.transition.duration")};
    outline-color: transparent;
}

.p-chip-remove-icon:focus-visible {
    box-shadow: ${e("chip.remove.icon.focus.ring.shadow")};
    outline: ${e("chip.remove.icon.focus.ring.width")} ${e("chip.remove.icon.focus.ring.style")} ${e("chip.remove.icon.focus.ring.color")};
    outline-offset: ${e("chip.remove.icon.focus.ring.offset")};
}
`,ge={root:"p-chip p-component",image:"p-chip-image",icon:"p-chip-icon",label:"p-chip-label",removeIcon:"p-chip-remove-icon"},Z=(()=>{class e extends W{name="chip";theme=he;classes=ge;static \u0275fac=(()=>{let i;return function(t){return(i||(i=y(e)))(t||e)}})();static \u0275prov=k({token:e,factory:e.\u0275fac})}return e})();var Fe=(()=>{class e extends X{label;icon;image;alt;style;styleClass;removable=!1;removeIcon;onRemove=new C;onImageError=new C;visible=!0;get removeAriaLabel(){return this.config.getTranslation(J.ARIA).removeLabel}get chipProps(){return this._chipProps}set chipProps(i){this._chipProps=i,i&&typeof i=="object"&&Object.entries(i).forEach(([n,t])=>this[`_${n}`]!==t&&(this[`_${n}`]=t))}_chipProps;_componentStyle=$(Z);removeIconTemplate;templates;_removeIconTemplate;ngAfterContentInit(){this.templates.forEach(i=>{switch(i.getType()){case"removeicon":this._removeIconTemplate=i.template;break;default:this._removeIconTemplate=i.template;break}})}ngOnChanges(i){if(super.ngOnChanges(i),i.chipProps&&i.chipProps.currentValue){let{currentValue:n}=i.chipProps;n.label!==void 0&&(this.label=n.label),n.icon!==void 0&&(this.icon=n.icon),n.image!==void 0&&(this.image=n.image),n.alt!==void 0&&(this.alt=n.alt),n.style!==void 0&&(this.style=n.style),n.styleClass!==void 0&&(this.styleClass=n.styleClass),n.removable!==void 0&&(this.removable=n.removable),n.removeIcon!==void 0&&(this.removeIcon=n.removeIcon)}}containerClass(){let i="p-chip p-component";return this.styleClass&&(i+=` ${this.styleClass}`),i}close(i){this.visible=!1,this.onRemove.emit(i)}onKeydown(i){(i.key==="Enter"||i.key==="Backspace")&&this.close(i)}imageError(i){this.onImageError.emit(i)}static \u0275fac=(()=>{let i;return function(t){return(i||(i=y(e)))(t||e)}})();static \u0275cmp=M({type:e,selectors:[["p-chip"]],contentQueries:function(n,t,o){if(n&1&&(I(o,ee,4),I(o,U,4)),n&2){let v;w(v=T())&&(t.removeIconTemplate=v.first),w(v=T())&&(t.templates=v)}},hostVars:9,hostBindings:function(n,t){n&2&&(_("data-pc-name","chip")("aria-label",t.label)("data-pc-section","root"),D(t.style),f(t.containerClass()),S("display",!t.visible&&"none"))},inputs:{label:"label",icon:"icon",image:"image",alt:"alt",style:"style",styleClass:"styleClass",removable:[2,"removable","removable",Q],removeIcon:"removeIcon",chipProps:"chipProps"},outputs:{onRemove:"onRemove",onImageError:"onImageError"},features:[B([Z]),P,E],ngContentSelectors:ie,decls:6,vars:4,consts:[["iconTemplate",""],["class","p-chip-image",3,"src","alt","error",4,"ngIf","ngIfElse"],["class","p-chip-label",4,"ngIf"],[4,"ngIf"],[1,"p-chip-image",3,"error","src","alt"],[3,"class","ngClass",4,"ngIf"],[3,"ngClass"],[1,"p-chip-label"],["tabindex","0","class","p-chip-remove-icon","role","button",3,"click","keydown",4,"ngIf"],["tabindex","0","role","button",3,"class","ngClass","click","keydown",4,"ngIf"],["tabindex","0","role","button",3,"class","click","keydown",4,"ngIf"],["tabindex","0","role","button",3,"click","keydown","ngClass"],["tabindex","0","role","button",3,"click","keydown"],["tabindex","0","role","button",1,"p-chip-remove-icon",3,"click","keydown"],[4,"ngTemplateOutlet"]],template:function(n,t){if(n&1&&(j(),z(0),m(1,ne,1,2,"img",1)(2,oe,1,1,"ng-template",null,0,N)(4,re,2,2,"div",2)(5,_e,3,2,"ng-container",3)),n&2){let o=A(3);p(),c("ngIf",t.image)("ngIfElse",o),p(3),c("ngIf",t.label),p(),c("ngIf",t.removable)}},dependencies:[H,K,L,q,Y,G],encapsulation:2,changeDetection:0})}return e})();export{Fe as a};
