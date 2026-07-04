import{a as N}from"./chunk-77Y2URMP.js";import{c as O,d as Q}from"./chunk-ERKC335S.js";import{Ga as d,Ja as j,Ma as z,j as D,m as L,s as T}from"./chunk-HT6JNWVA.js";import{Ab as S,Gb as u,Gc as g,Hb as p,Hc as B,Ib as I,Mb as _,Rb as w,V as f,W as m,Wa as a,Wb as M,X as k,Xb as F,Yb as E,aa as y,gb as v,ha as n,hb as C,hc as A,ia as r,kb as V,la as h,ra as $,vb as l,wb as c}from"./chunk-SQFFUPMZ.js";var P=["input"],W=({dt:e})=>`
.p-toggleswitch {
    display: inline-block;
    width: ${e("toggleswitch.width")};
    height: ${e("toggleswitch.height")};

}

.p-toggleswitch-input {
    cursor: pointer;
    appearance: none;
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    padding: 0;
    margin: 0;
    opacity: 0;
    z-index: 1;
    outline: 0 none;
    border-radius: ${e("toggleswitch.border.radius")};
}

.p-toggleswitch-slider {
    display: inline-block;
    cursor: pointer;
    width: 100%;
    height: 100%;
    border-width: ${e("toggleswitch.border.width")};
    border-style: solid;
    border-color: ${e("toggleswitch.border.color")};
    background: ${e("toggleswitch.background")};
    transition: background ${e("toggleswitch.transition.duration")}, color ${e("toggleswitch.transition.duration")}, border-color ${e("toggleswitch.transition.duration")}, outline-color ${e("toggleswitch.transition.duration")}, box-shadow ${e("toggleswitch.transition.duration")};
    border-radius: ${e("toggleswitch.border.radius")};
    outline-color: transparent;
    box-shadow: ${e("toggleswitch.shadow")};
}

.p-toggleswitch-slider:before {
    position: absolute;
    content: "";
    top: 50%;
    background: ${e("toggleswitch.handle.background")};
    width: ${e("toggleswitch.handle.size")};
    height: ${e("toggleswitch.handle.size")};
    left: ${e("toggleswitch.gap")};
    margin-top: calc(-1 * calc(${e("toggleswitch.handle.size")} / 2));
    border-radius: ${e("toggleswitch.handle.border.radius")};
    transition: background ${e("toggleswitch.transition.duration")}, left ${e("toggleswitch.slide.duration")};
}

.p-toggleswitch.p-toggleswitch-checked .p-toggleswitch-slider {
    background: ${e("toggleswitch.checked.background")};
    border-color: ${e("toggleswitch.checked.border.color")};
}

.p-toggleswitch.p-toggleswitch-checked .p-toggleswitch-slider:before {
    background: ${e("toggleswitch.handle.checked.background")};
    left: calc(${e("toggleswitch.width")} - calc(${e("toggleswitch.handle.size")} + ${e("toggleswitch.gap")}));
}

.p-toggleswitch:not(.p-disabled):has(.p-toggleswitch-input:hover) .p-toggleswitch-slider {
    background: ${e("toggleswitch.hover.background")};
    border-color: ${e("toggleswitch.hover.border.color")};
}

.p-toggleswitch:not(.p-disabled):has(.p-toggleswitch-input:hover) .p-toggleswitch-slider:before {
    background: ${e("toggleswitch.handle.hover.background")};
}

.p-toggleswitch:not(.p-disabled):has(.p-toggleswitch-input:hover).p-toggleswitch-checked .p-toggleswitch-slider {
    background: ${e("toggleswitch.checked.hover.background")};
    border-color: ${e("toggleswitch.checked.hover.border.color")};
}

.p-toggleswitch:not(.p-disabled):has(.p-toggleswitch-input:hover).p-toggleswitch-checked .p-toggleswitch-slider:before {
    background: ${e("toggleswitch.handle.checked.hover.background")};
}

.p-toggleswitch:not(.p-disabled):has(.p-toggleswitch-input:focus-visible) .p-toggleswitch-slider {
    box-shadow: ${e("toggleswitch.focus.ring.shadow")};
    outline: ${e("toggleswitch.focus.ring.width")} ${e("toggleswitch.focus.ring.style")} ${e("toggleswitch.focus.ring.color")};
    outline-offset: ${e("toggleswitch.focus.ring.offset")};
}

.p-toggleswitch.p-invalid > .p-toggleswitch-slider {
    border-color: ${e("toggleswitch.invalid.border.color")};
}

.p-toggleswitch.p-disabled {
    opacity: 1;
}

.p-toggleswitch.p-disabled .p-toggleswitch-slider {
    background: ${e("toggleswitch.disabled.background")};
}

.p-toggleswitch.p-disabled .p-toggleswitch-slider:before {
    background: ${e("toggleswitch.handle.disabled.background")};
}
`,q={root:{position:"relative"}},G={root:({instance:e})=>({"p-toggleswitch p-component":!0,"p-toggleswitch-checked":e.checked(),"p-disabled":e.disabled,"p-invalid":e.invalid}),input:"p-toggleswitch-input",slider:"p-toggleswitch-slider"},R=(()=>{class e extends j{name="toggleswitch";theme=W;classes=G;inlineStyles=q;static \u0275fac=(()=>{let i;return function(t){return(i||(i=h(e)))(t||e)}})();static \u0275prov=m({token:e,factory:e.\u0275fac})}return e})(),H={provide:N,useExisting:f(()=>U),multi:!0},U=(()=>{class e extends z{style;styleClass;tabindex;inputId;name;disabled;readonly;trueValue=!0;falseValue=!1;ariaLabel;ariaLabelledBy;autofocus;onChange=new $;input;modelValue=!1;focused=!1;onModelChange=()=>{};onModelTouched=()=>{};_componentStyle=y(R);onClick(i){!this.disabled&&!this.readonly&&(this.modelValue=this.checked()?this.falseValue:this.trueValue,this.onModelChange(this.modelValue),this.onChange.emit({originalEvent:i,checked:this.modelValue}),this.input.nativeElement.focus())}onFocus(){this.focused=!0}onBlur(){this.focused=!1,this.onModelTouched()}writeValue(i){this.modelValue=i,this.cd.markForCheck()}registerOnChange(i){this.onModelChange=i}registerOnTouched(i){this.onModelTouched=i}setDisabledState(i){this.disabled=i,this.cd.markForCheck()}checked(){return this.modelValue===this.trueValue}static \u0275fac=(()=>{let i;return function(t){return(i||(i=h(e)))(t||e)}})();static \u0275cmp=v({type:e,selectors:[["p-inputSwitch"],["p-inputswitch"]],viewQuery:function(o,t){if(o&1&&M(P,5),o&2){let s;F(s=E())&&(t.input=s.first)}},inputs:{style:"style",styleClass:"styleClass",tabindex:[2,"tabindex","tabindex",B],inputId:"inputId",name:"name",disabled:[2,"disabled","disabled",g],readonly:[2,"readonly","readonly",g],trueValue:"trueValue",falseValue:"falseValue",ariaLabel:"ariaLabel",ariaLabelledBy:"ariaLabelledBy",autofocus:[2,"autofocus","autofocus",g]},outputs:{onChange:"onChange"},features:[A([H,R]),V],decls:5,vars:22,consts:[["input",""],[3,"click","ngClass","ngStyle"],[1,"p-hidden-accessible"],["type","checkbox","role","switch",3,"focus","blur","ngClass","checked","disabled","pAutoFocus"],[3,"ngClass"]],template:function(o,t){if(o&1){let s=_();u(0,"div",1),w("click",function(x){return n(s),r(t.onClick(x))}),u(1,"div",2)(2,"input",3,0),w("focus",function(){return n(s),r(t.onFocus())})("blur",function(){return n(s),r(t.onBlur())}),p()(),I(4,"span",4),p()}o&2&&(S(t.styleClass),c("ngClass",t.cx("root"))("ngStyle",t.sx("root"))("ngStyle",t.style),l("data-pc-name","inputswitch")("data-pc-section","root"),a(),l("data-pc-section","hiddenInputWrapper")("data-p-hidden-accessible",!0),a(),c("ngClass",t.cx("input"))("checked",t.checked())("disabled",t.disabled)("pAutoFocus",t.autofocus),l("id",t.inputId)("aria-checked",t.checked())("aria-labelledby",t.ariaLabelledBy)("aria-label",t.ariaLabel)("name",t.name)("tabindex",t.tabindex)("data-pc-section","hiddenInput"),a(2),c("ngClass",t.cx("slider")),l("data-pc-section","slider"))},dependencies:[T,D,L,Q,O,d],encapsulation:2,changeDetection:0})}return e})(),ue=(()=>{class e{static \u0275fac=function(o){return new(o||e)};static \u0275mod=C({type:e});static \u0275inj=k({imports:[U,d,d]})}return e})();export{U as a,ue as b};
