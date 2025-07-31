# capacitor-photo-editor

PhotoEditor Capacitor

## Install

```bash
npm install capacitor-photo-editor
npx cap sync
```

## API

<docgen-index>

* [`echo(...)`](#echo)
* [`editPhoto(...)`](#editphoto)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### echo(...)

```typescript
echo(options: { value: string; }) => Promise<{ value: string; }>
```

| Param         | Type                            |
| ------------- | ------------------------------- |
| **`options`** | <code>{ value: string; }</code> |

**Returns:** <code>Promise&lt;{ value: string; }&gt;</code>

--------------------


### editPhoto(...)

```typescript
editPhoto(options: EditPhotoOptions) => Promise<EditPhotoResult>
```

| Param         | Type                                                          |
| ------------- | ------------------------------------------------------------- |
| **`options`** | <code><a href="#editphotooptions">EditPhotoOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#editphotoresult">EditPhotoResult</a>&gt;</code>

--------------------


### Interfaces


#### EditPhotoResult

| Prop        | Type                |
| ----------- | ------------------- |
| **`image`** | <code>string</code> |


#### EditPhotoOptions

| Prop        | Type                |
| ----------- | ------------------- |
| **`image`** | <code>string</code> |

</docgen-api>
